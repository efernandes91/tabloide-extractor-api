package br.com.eduardo.tabloideapi.dto;

import java.util.List;

public record CategoriaResponse(
        String nome,
        List<ProdutoResponse> produtos
) {
}
