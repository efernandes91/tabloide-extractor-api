package br.com.eduardo.tabloideapi.dto;

import java.math.BigDecimal;

public record VisionOffer(
        String nome,
        String marca,
        BigDecimal quantidade,
        String unidade,
        BigDecimal preco,
        String categoria,
        String textoOriginal
) {
}
