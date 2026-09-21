package br.com.eduardo.tabloideapi.service;

import br.com.eduardo.tabloideapi.dto.VisionOffer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OfferMergerTests {

    private final OfferMerger merger = new OfferMerger();

    @Test
    void removeOfertaDuplicadaPresenteNaSobreposicaoDosRecortes() {
        VisionOffer first = offer("LEITE UHT LIDER", "5.99");
        VisionOffer duplicate = offer("Leite UHT Líder Integral", "5.99");

        List<VisionOffer> result = merger.merge(List.of(first, duplicate));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().nome()).isEqualTo("Leite UHT Líder Integral");
    }

    @Test
    void preservaProdutosDiferentesComMesmoPreco() {
        List<VisionOffer> result = merger.merge(List.of(
                offer("LEITE UHT LIDER", "5.99"),
                offer("LEITE CONDENSADO TRIANGULO", "5.99")
        ));

        assertThat(result).hasSize(2);
    }

    @Test
    void consolidaLeiturasDivergentesComMesmaMarcaMedidaEPreco() {
        VisionOffer correct = new VisionOffer(
                "MACARRÃO PAULISTA SEMOLA",
                "Paulista",
                new BigDecimal("500"),
                "g",
                new BigDecimal("2.99"),
                "Mercearia",
                "MACARRÃO PAULISTA SEMOLA 500GR CLUBE REDEPAS R$2,99"
        );
        VisionOffer wrong = new VisionOffer(
                "PASTA DE DENTE PAULISTA SEMOLA",
                "Paulista",
                new BigDecimal("500"),
                "g",
                new BigDecimal("2.99"),
                "Mercearia",
                "PAULISTA SEMOLA 500GR"
        );

        List<VisionOffer> result = merger.merge(List.of(correct, wrong));

        assertThat(result).containsExactly(correct);
    }

    private VisionOffer offer(String name, String price) {
        return new VisionOffer(
                name,
                null,
                null,
                null,
                new BigDecimal(price),
                "Laticínios",
                name
        );
    }
}
