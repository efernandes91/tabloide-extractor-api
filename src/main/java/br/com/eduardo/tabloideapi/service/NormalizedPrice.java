package br.com.eduardo.tabloideapi.service;

import java.math.BigDecimal;

public record NormalizedPrice(
        BigDecimal value,
        String unit
) {
}
