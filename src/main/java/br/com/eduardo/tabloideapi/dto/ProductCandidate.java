package br.com.eduardo.tabloideapi.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductCandidate(
        BigDecimal price,
        List<OcrWord> words
) {
}