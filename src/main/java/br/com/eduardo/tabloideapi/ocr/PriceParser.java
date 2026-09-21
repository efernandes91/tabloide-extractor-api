package br.com.eduardo.tabloideapi.ocr;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PriceParser {

    private static final Pattern DECIMAL_PATTERN =
            Pattern.compile("(\\d+)[,.](\\d{2})");

    public Optional<BigDecimal> parse(String text) {

        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        String normalized = text.trim();

        Matcher matcher = DECIMAL_PATTERN.matcher(normalized);

        if (matcher.find()) {
            return Optional.of(
                    new BigDecimal(
                            matcher.group(1) + "." + matcher.group(2)
                    )
            );
        }

        String digits = normalized.replaceAll("\\D", "");

        if (digits.length() < 3) {
            return Optional.empty();
        }

        String value =
                digits.substring(0, digits.length() - 2)
                        + "."
                        + digits.substring(digits.length() - 2);

        return Optional.of(new BigDecimal(value));
    }
}