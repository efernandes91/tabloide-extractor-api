package br.com.eduardo.tabloideapi.dto;

public record ApiStatusResponse(
        String status,
        String mensagem,
        String processamento
) {
}
