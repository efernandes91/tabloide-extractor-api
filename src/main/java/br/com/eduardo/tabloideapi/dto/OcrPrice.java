package br.com.eduardo.tabloideapi.dto;

import java.math.BigDecimal;

public record OcrPrice(
        BigDecimal value,
        int x,
        int y
) {
}