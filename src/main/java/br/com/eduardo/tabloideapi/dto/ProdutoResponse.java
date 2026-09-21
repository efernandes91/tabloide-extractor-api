package br.com.eduardo.tabloideapi.dto;

import java.math.BigDecimal;

public record ProdutoResponse(
        String nome,
        String marca,
        BigDecimal quantidade,
        String unidade,
        BigDecimal preco,
        BigDecimal precoNormalizado,
        String unidadeNormalizada,
        String textoOriginal
) {
}
