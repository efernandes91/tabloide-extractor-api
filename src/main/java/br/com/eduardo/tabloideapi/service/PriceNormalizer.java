package br.com.eduardo.tabloideapi.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

@Component
public class PriceNormalizer {

    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

    public Optional<NormalizedPrice> normalize(
            BigDecimal price,
            BigDecimal quantity,
            String unit
    ) {
        if (price == null || quantity == null || quantity.signum() <= 0 || unit == null) {
            return Optional.empty();
        }

        String normalizedUnit = normalizeUnit(unit);

        return switch (normalizedUnit) {
            case "g" -> Optional.of(result(price.multiply(THOUSAND).divide(quantity, 4, RoundingMode.HALF_UP), "kg"));
            case "kg" -> Optional.of(result(price.divide(quantity, 4, RoundingMode.HALF_UP), "kg"));
            case "ml" -> Optional.of(result(price.multiply(THOUSAND).divide(quantity, 4, RoundingMode.HALF_UP), "l"));
            case "l" -> Optional.of(result(price.divide(quantity, 4, RoundingMode.HALF_UP), "l"));
            case "un" -> Optional.of(result(price.divide(quantity, 4, RoundingMode.HALF_UP), "un"));
            default -> Optional.empty();
        };
    }

    public String normalizeUnit(String unit) {
        if (unit == null) {
            return null;
        }

        String value = Normalizer.normalize(unit, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace(".", "")
                .trim();

        return switch (value) {
            case "gr", "grama", "gramas" -> "g";
            case "quilo", "quilos" -> "kg";
            case "litro", "litros", "lt" -> "l";
            case "mililitro", "mililitros" -> "ml";
            case "unidade", "unidades", "und" -> "un";
            default -> value;
        };
    }

    private NormalizedPrice result(BigDecimal value, String unit) {
        return new NormalizedPrice(
                value.setScale(2, RoundingMode.HALF_UP),
                unit
        );
    }
}
