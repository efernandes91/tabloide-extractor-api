package br.com.eduardo.tabloideapi.service;

import br.com.eduardo.tabloideapi.dto.VisionOffer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class VisionOfferSanitizerTests {

    private final VisionOfferSanitizer sanitizer =
            new VisionOfferSanitizer(new PriceNormalizer());

    @Test
    void textoOriginalCorrigeMedidaEPrecoEstruturadosIncorretamente() {
        VisionOffer result = sanitizer.sanitize(new VisionOffer(
                "SABONETE BRASILIDADES",
                "Brasilidades",
                new BigDecimal("800"),
                "ml",
                new BigDecimal("3.99"),
                "Higiene e limpeza",
                "SABONETE BRASILIDADES 80G R$2,99"
        ));

        assertThat(result.quantidade()).isEqualByComparingTo("80");
        assertThat(result.unidade()).isEqualTo("g");
        assertThat(result.preco()).isEqualByComparingTo("2.99");
    }

    @Test
    void rejeitaUnidadeForaDoContrato() {
        VisionOffer result = sanitizer.sanitize(new VisionOffer(
                "Produto",
                null,
                BigDecimal.ONE,
                "caixa",
                BigDecimal.TEN,
                null,
                null
        ));

        assertThat(result.unidade()).isNull();
        assertThat(result.categoria()).isEqualTo("Outros");
    }

    @Test
    void converteStringNullEmNuloReal() {
        VisionOffer result = sanitizer.sanitize(new VisionOffer(
                "Pão Francês",
                "null",
                null,
                "kg",
                new BigDecimal("14.99"),
                "Padaria",
                "Pão Francês R$14,99"
        ));

        assertThat(result.marca()).isNull();
    }
}
