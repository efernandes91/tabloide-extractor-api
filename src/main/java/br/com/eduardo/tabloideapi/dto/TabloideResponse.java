package br.com.eduardo.tabloideapi.dto;

import java.util.List;

public record TabloideResponse(
        String mercado,
        ValidadeResponse validade,
        List<CategoriaResponse> categorias
) {
}
