package br.com.eduardo.tabloideapi.extractor;

import br.com.eduardo.tabloideapi.config.TabloideProperties;
import br.com.eduardo.tabloideapi.dto.CategoriaResponse;
import br.com.eduardo.tabloideapi.dto.ProdutoResponse;
import br.com.eduardo.tabloideapi.dto.TabloideResponse;
import br.com.eduardo.tabloideapi.dto.ValidadeResponse;
import br.com.eduardo.tabloideapi.dto.VisionHeader;
import br.com.eduardo.tabloideapi.dto.VisionOffer;
import br.com.eduardo.tabloideapi.dto.VisionOffers;
import br.com.eduardo.tabloideapi.image.TabloideImageSegmenter;
import br.com.eduardo.tabloideapi.service.NormalizedPrice;
import br.com.eduardo.tabloideapi.service.OfferMerger;
import br.com.eduardo.tabloideapi.service.PriceNormalizer;
import br.com.eduardo.tabloideapi.service.VisionOfferSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LocalVisionTabloideExtractor implements TabloideExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalVisionTabloideExtractor.class);

    private static final DateTimeFormatter BRAZILIAN_DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private static final String HEADER_PROMPT = """
            Leia somente o cabeçalho deste tabloide brasileiro.
            Extraia o nome exato do supermercado e o período completo da oferta.
            Observe cuidadosamente todos os dígitos das datas.
            Converta início e fim para yyyy-MM-dd.
            Use null quando uma informação realmente não estiver visível.
            Não extraia produtos.
            """;

    private static final String OFFERS_PROMPT = """
            Esta imagem é uma região do corpo de um tabloide brasileiro.
            Extraia todas as ofertas suficientemente visíveis nesta região.

            Regras obrigatórias:
            - Não invente informações nem complete texto ilegível por suposição.
            - Associe cada preço somente ao produto visualmente correspondente.
            - Separe marca do nome do produto.
            - Separe número e unidade: 350ML significa quantidade 350 e unidade ml.
            - As unidades permitidas são apenas g, kg, ml, l e un.
            - Use null verdadeiro quando quantidade, unidade ou preço não estiverem legíveis.
            - Preço é um número decimal em reais, sem o símbolo R$.
            - Categoria deve ser uma entre: Bebidas, Mercearia, Laticínios, Higiene e limpeza, Padaria ou Outros.
            - textoOriginal deve reproduzir literalmente o texto visível associado à oferta, incluindo medida e preço.
            - Não use informações de produtos cortados se não for possível associar nome e preço com segurança.
            """;

    private static final String HEADER_SCHEMA = """
            {
              "type": "object",
              "additionalProperties": false,
              "properties": {
                "mercado": {"type": ["string", "null"]},
                "validadeTexto": {"type": ["string", "null"]},
                "inicio": {"type": ["string", "null"]},
                "fim": {"type": ["string", "null"]}
              },
              "required": ["mercado", "validadeTexto", "inicio", "fim"]
            }
            """;

    private static final String OFFERS_SCHEMA = """
            {
              "type": "object",
              "additionalProperties": false,
              "properties": {
                "ofertas": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "additionalProperties": false,
                    "properties": {
                      "nome": {"type": "string"},
                      "marca": {"type": ["string", "null"]},
                      "quantidade": {"type": ["number", "null"]},
                      "unidade": {
                        "type": ["string", "null"],
                        "enum": ["g", "kg", "ml", "l", "un", null]
                      },
                      "preco": {"type": ["number", "null"]},
                      "categoria": {
                        "type": ["string", "null"],
                        "enum": ["Bebidas", "Mercearia", "Laticínios", "Higiene e limpeza", "Padaria", "Outros", null]
                      },
                      "textoOriginal": {"type": ["string", "null"]}
                    },
                    "required": ["nome", "marca", "quantidade", "unidade", "preco", "categoria", "textoOriginal"]
                  }
                }
              },
              "required": ["ofertas"]
            }
            """;

    private final OllamaVisionClient ollamaClient;
    private final TabloideImageSegmenter imageSegmenter;
    private final OfferMerger offerMerger;
    private final VisionOfferSanitizer offerSanitizer;
    private final PriceNormalizer priceNormalizer;
    private final TabloideProperties.Ollama properties;

    public LocalVisionTabloideExtractor(
            OllamaVisionClient ollamaClient,
            TabloideImageSegmenter imageSegmenter,
            OfferMerger offerMerger,
            VisionOfferSanitizer offerSanitizer,
            PriceNormalizer priceNormalizer,
            TabloideProperties properties
    ) {
        this.ollamaClient = ollamaClient;
        this.imageSegmenter = imageSegmenter;
        this.offerMerger = offerMerger;
        this.offerSanitizer = offerSanitizer;
        this.priceNormalizer = priceNormalizer;
        this.properties = properties.ollama();
    }

    @Override
    public String type() {
        return "local-vision";
    }

    @Override
    public TabloideResponse extract(BufferedImage image) {
        LOGGER.info("Iniciando leitura do cabeçalho do tabloide");
        VisionHeader header = ollamaClient.extract(
                imageSegmenter.header(image),
                HEADER_PROMPT,
                HEADER_SCHEMA,
                properties.headerMaxTokens(),
                VisionHeader.class
        );

        List<VisionOffer> extractedOffers = new ArrayList<>();

        List<BufferedImage> bodyTiles = imageSegmenter.bodyTiles(image);
        for (int index = 0; index < bodyTiles.size(); index++) {
            LOGGER.info("Extraindo ofertas da região {}/{}", index + 1, bodyTiles.size());
            VisionOffers response = ollamaClient.extract(
                    bodyTiles.get(index),
                    OFFERS_PROMPT,
                    OFFERS_SCHEMA,
                    properties.offersMaxTokens(),
                    VisionOffers.class
            );

            if (response.ofertas() != null) {
                response.ofertas().stream()
                        .map(offerSanitizer::sanitize)
                        .filter(this::isUseful)
                        .forEach(extractedOffers::add);
            }
        }

        List<VisionOffer> offers = offerMerger.merge(extractedOffers);
        LOGGER.info("Extração concluída: {} leituras, {} ofertas após deduplicação",
                extractedOffers.size(), offers.size());

        Map<String, List<ProdutoResponse>> byCategory = offers.stream()
                .collect(Collectors.groupingBy(
                        VisionOffer::categoria,
                        LinkedHashMap::new,
                        Collectors.mapping(this::toProduct, Collectors.toList())
                ));

        List<CategoriaResponse> categories = byCategory.entrySet().stream()
                .map(entry -> new CategoriaResponse(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();

        return new TabloideResponse(
                trimToNull(header.mercado()),
                new ValidadeResponse(parseDate(header.inicio()), parseDate(header.fim())),
                categories
        );
    }

    private boolean isUseful(VisionOffer offer) {
        return offer != null && offer.nome() != null && !offer.nome().isBlank();
    }

    private ProdutoResponse toProduct(VisionOffer offer) {
        Optional<NormalizedPrice> normalized = priceNormalizer.normalize(
                offer.preco(),
                offer.quantidade(),
                offer.unidade()
        );

        return new ProdutoResponse(
                offer.nome(),
                offer.marca(),
                offer.quantidade(),
                offer.unidade(),
                offer.preco(),
                normalized.map(NormalizedPrice::value).orElse(null),
                normalized.map(NormalizedPrice::unit).orElse(null),
                offer.textoOriginal()
        );
    }

    private LocalDate parseDate(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }

        for (DateTimeFormatter formatter : List.of(DateTimeFormatter.ISO_LOCAL_DATE, BRAZILIAN_DATE)) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // Tenta o próximo formato conhecido.
            }
        }

        return null;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
