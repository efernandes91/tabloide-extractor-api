package br.com.eduardo.tabloideapi.dto;

import java.util.List;

public record OcrResult(
        String rawText,
        List<OcrWord> words
) {
}
