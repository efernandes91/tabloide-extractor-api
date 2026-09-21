package br.com.eduardo.tabloideapi.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PriceNormalizerTests {

    private final PriceNormalizer normalizer = new PriceNormalizer();

    @Test
    void normalizaGramasParaPrecoPorQuilo() {
        NormalizedPrice result = normalizer.normalize(
                new BigDecimal("2.99"),
                new BigDecimal("500"),
                "g"
        ).orElseThrow();

        assertThat(result.value()).isEqualByComparingTo("5.98");
        assertThat(result.unit()).isEqualTo("kg");
    }

    @Test
    void normalizaMililitrosParaPrecoPorLitro() {
        NormalizedPrice result = normalizer.normalize(
                new BigDecimal("7.59"),
                new BigDecimal("900"),
                "ml"
        ).orElseThrow();

        assertThat(result.value()).isEqualByComparingTo("8.43");
        assertThat(result.unit()).isEqualTo("l");
    }

    @Test
    void naoNormalizaSemQuantidade() {
        assertThat(normalizer.normalize(new BigDecimal("2.99"), null, "g"))
                .isEmpty();
    }
}
