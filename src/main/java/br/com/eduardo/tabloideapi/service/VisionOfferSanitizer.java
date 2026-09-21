package br.com.eduardo.tabloideapi.service;

import br.com.eduardo.tabloideapi.dto.VisionOffer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VisionOfferSanitizer {

    private static final Set<String> ALLOWED_UNITS = Set.of("g", "kg", "ml", "l", "un");
    private static final Pattern MEASURE_PATTERN = Pattern.compile(
            "(?i)(?<!\\d)(\\d+(?:[,.]\\d+)?)\\s*(kg|gr|g|ml|lt|l)(?![\\p{L}])"
    );
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "(?i)R\\$\\s*(\\d{1,4})[,.](\\d{2})"
    );

    private final PriceNormalizer priceNormalizer;

    public VisionOfferSanitizer(PriceNormalizer priceNormalizer) {
        this.priceNormalizer = priceNormalizer;
    }

    public VisionOffer sanitize(VisionOffer offer) {
        if (offer == null) {
            return null;
        }

        BigDecimal quantity = positiveOrNull(offer.quantidade());
        String unit = normalizeAllowedUnit(offer.unidade());
        BigDecimal price = positiveOrNull(offer.preco());
        String originalText = trimToNull(offer.textoOriginal());

        if (originalText != null) {
            Matcher measureMatcher = MEASURE_PATTERN.matcher(originalText);
            if (measureMatcher.find()) {
                quantity = decimal(measureMatcher.group(1));
                unit = normalizeAllowedUnit(measureMatcher.group(2));
            }

            Matcher priceMatcher = PRICE_PATTERN.matcher(originalText);
            if (priceMatcher.find()) {
                price = new BigDecimal(priceMatcher.group(1) + "." + priceMatcher.group(2));
            }
        }

        return new VisionOffer(
                trimToNull(offer.nome()),
                trimToNull(offer.marca()),
                quantity,
                unit,
                price,
                normalizeCategory(offer.categoria()),
                originalText
        );
    }

    private BigDecimal positiveOrNull(BigDecimal value) {
        return value != null && value.signum() > 0 ? value : null;
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value.replace(',', '.'));
    }

    private String normalizeAllowedUnit(String value) {
        String normalized = priceNormalizer.normalizeUnit(value);
        return ALLOWED_UNITS.contains(normalized) ? normalized : null;
    }

    private String normalizeCategory(String value) {
        String category = trimToNull(value);
        if (category == null) {
            return "Outros";
        }

        return switch (category.toLowerCase(Locale.ROOT)) {
            case "bebidas" -> "Bebidas";
            case "mercearia" -> "Mercearia";
            case "laticínios", "laticinios" -> "Laticínios";
            case "higiene e limpeza" -> "Higiene e limpeza";
            case "padaria" -> "Padaria";
            default -> "Outros";
        };
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
