package br.com.eduardo.tabloideapi.ocr;

import br.com.eduardo.tabloideapi.dto.OcrWord;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OcrFilter {

    private static final float MIN_CONFIDENCE = 60.0f;

    public List<OcrWord> filter(List<OcrWord> words) {

        return words.stream()
                .filter(word ->
                        word.confidence() >= MIN_CONFIDENCE
                                || looksLikePrice(word.text())
                )
                .toList();
    }

    private boolean looksLikePrice(String text) {

        if (text == null) {
            return false;
        }

        String normalized = text
                .replace(" ", "")
                .toUpperCase();

        return normalized.matches(".*\\d+[,.]\\d{2}.*");
    }
}