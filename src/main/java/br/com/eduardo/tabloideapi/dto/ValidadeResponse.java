package br.com.eduardo.tabloideapi.dto;

import java.time.LocalDate;

public record ValidadeResponse(
        LocalDate inicio,
        LocalDate fim
) {
}
