package br.com.eduardo.tabloideapi.ocr;

import br.com.eduardo.tabloideapi.dto.OcrPrice;
import br.com.eduardo.tabloideapi.dto.OcrWord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PriceDetector {

    private static final Pattern PRICE_PATTERN =
            Pattern.compile(
                    "(?<!\\d)(\\d{1,3})[,.](\\d{2})(?![\\d\\p{L}])"
            );

    public List<OcrPrice> detect(List<OcrWord> words) {

        return words.stream()
                .map(this::toPrice)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<OcrPrice> toPrice(OcrWord word) {

        Matcher matcher = PRICE_PATTERN.matcher(word.text());

        if (!matcher.find()) {
            return Optional.empty();
        }

        String value =
                matcher.group(1) + "." + matcher.group(2);

        return Optional.of(
                new OcrPrice(
                        new BigDecimal(value),
                        word.centerX(),
                        word.centerY()
                )
        );
    }
}