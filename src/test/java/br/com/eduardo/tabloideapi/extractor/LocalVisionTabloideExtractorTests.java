package br.com.eduardo.tabloideapi.extractor;

import br.com.eduardo.tabloideapi.config.TabloideProperties;
import br.com.eduardo.tabloideapi.dto.TabloideResponse;
import br.com.eduardo.tabloideapi.dto.VisionHeader;
import br.com.eduardo.tabloideapi.dto.VisionOffer;
import br.com.eduardo.tabloideapi.dto.VisionOffers;
import br.com.eduardo.tabloideapi.exception.OllamaResponseTruncatedException;
import br.com.eduardo.tabloideapi.exception.TabloideExtractionException;
import br.com.eduardo.tabloideapi.image.TabloideImageSegmenter;
import br.com.eduardo.tabloideapi.progress.ExtractionProgressLogger;
import br.com.eduardo.tabloideapi.service.OfferMerger;
import br.com.eduardo.tabloideapi.service.PriceNormalizer;
import br.com.eduardo.tabloideapi.service.VisionOfferSanitizer;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LocalVisionTabloideExtractorTests {

    @Test
    void subdivideRegiaoTruncadaEPreservaOfertasDosFilhos() {
        OllamaVisionClient ollamaClient = mock(OllamaVisionClient.class);
        TabloideProperties properties = propertiesWithOneBodyTile();
        PriceNormalizer priceNormalizer = new PriceNormalizer();

        doReturn(new VisionHeader("Mercado Teste", null, null, null))
                .when(ollamaClient)
                .extract(any(), anyString(), anyString(), anyInt(), eq(VisionHeader.class));

        doThrow(new OllamaResponseTruncatedException(1800, 1800))
                .doReturn(new VisionOffers(List.of(offer("Arroz", "10.00"))))
                .doReturn(new VisionOffers(List.of(offer("Feijão", "8.00"))))
                .when(ollamaClient)
                .extract(any(), anyString(), anyString(), anyInt(), eq(VisionOffers.class));

        try (ExtractionProgressLogger progressLogger = new ExtractionProgressLogger()) {
            LocalVisionTabloideExtractor extractor = new LocalVisionTabloideExtractor(
                    ollamaClient,
                    new TabloideImageSegmenter(properties),
                    new OfferMerger(),
                    new VisionOfferSanitizer(priceNormalizer),
                    priceNormalizer,
                    progressLogger,
                    properties
            );

            TabloideResponse result = extractor.extract(
                    new BufferedImage(200, 300, BufferedImage.TYPE_INT_RGB)
            );

            assertThat(result.mercado()).isEqualTo("Mercado Teste");
            assertThat(result.categorias())
                    .flatExtracting(category -> category.produtos())
                    .extracting(product -> product.nome())
                    .containsExactly("Arroz", "Feijão");
        }

        verify(ollamaClient, times(3))
                .extract(any(), anyString(), anyString(), anyInt(), eq(VisionOffers.class));
    }

    @Test
    void interrompeRetriesQuandoAProfundidadeMaximaForAtingida() {
        OllamaVisionClient ollamaClient = mock(OllamaVisionClient.class);
        TabloideProperties properties = propertiesWithOneBodyTile();
        PriceNormalizer priceNormalizer = new PriceNormalizer();

        doReturn(new VisionHeader("Mercado Teste", null, null, null))
                .when(ollamaClient)
                .extract(any(), anyString(), anyString(), anyInt(), eq(VisionHeader.class));
        doThrow(new OllamaResponseTruncatedException(1800, 1800))
                .when(ollamaClient)
                .extract(any(), anyString(), anyString(), anyInt(), eq(VisionOffers.class));

        try (ExtractionProgressLogger progressLogger = new ExtractionProgressLogger()) {
            LocalVisionTabloideExtractor extractor = new LocalVisionTabloideExtractor(
                    ollamaClient,
                    new TabloideImageSegmenter(properties),
                    new OfferMerger(),
                    new VisionOfferSanitizer(priceNormalizer),
                    priceNormalizer,
                    progressLogger,
                    properties
            );

            assertThatThrownBy(() -> extractor.extract(
                    new BufferedImage(200, 300, BufferedImage.TYPE_INT_RGB)
            ))
                    .isInstanceOf(TabloideExtractionException.class)
                    .hasMessageContaining("mesmo após subdividi-la");
        }

        verify(ollamaClient, times(2))
                .extract(any(), anyString(), anyString(), anyInt(), eq(VisionOffers.class));
    }

    private TabloideProperties propertiesWithOneBodyTile() {
        return new TabloideProperties(
                "local-vision",
                new TabloideProperties.Ollama(
                        "http://localhost:11434",
                        "qwen3-vl:4b-instruct",
                        8192,
                        512,
                        1800
                ),
                new TabloideProperties.Image(0.20, 1, 1, 0.12)
        );
    }

    private VisionOffer offer(String name, String price) {
        return new VisionOffer(
                name,
                null,
                BigDecimal.ONE,
                "kg",
                new BigDecimal(price),
                "Mercearia",
                name + " 1kg R$" + price.replace('.', ',')
        );
    }
}
