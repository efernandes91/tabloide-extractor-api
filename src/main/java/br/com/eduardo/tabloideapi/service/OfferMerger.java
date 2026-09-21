package br.com.eduardo.tabloideapi.service;

import br.com.eduardo.tabloideapi.dto.VisionOffer;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OfferMerger {

    public List<VisionOffer> merge(List<VisionOffer> offers) {
        List<VisionOffer> merged = new ArrayList<>();

        for (VisionOffer candidate : offers) {
            int duplicateIndex = findDuplicate(merged, candidate);

            if (duplicateIndex < 0) {
                merged.add(candidate);
            } else if (score(candidate) > score(merged.get(duplicateIndex))) {
                merged.set(duplicateIndex, candidate);
            }
        }

        return List.copyOf(merged);
    }

    private int findDuplicate(List<VisionOffer> offers, VisionOffer candidate) {
        for (int index = 0; index < offers.size(); index++) {
            if (isDuplicate(offers.get(index), candidate)) {
                return index;
            }
        }
        return -1;
    }

    private boolean isDuplicate(VisionOffer first, VisionOffer second) {
        if (first.preco() == null || second.preco() == null
                || first.preco().compareTo(second.preco()) != 0) {
            return false;
        }

        String firstName = normalizeText(first.nome());
        String secondName = normalizeText(second.nome());

        if (firstName.isBlank() || secondName.isBlank()) {
            return false;
        }

        if (firstName.equals(secondName)
                || firstName.contains(secondName)
                || secondName.contains(firstName)) {
            return true;
        }

        if (hasSameMeasure(first, second)
                && hasEquivalentBrand(first.marca(), second.marca())
                && sharedTokenCount(firstName, secondName) >= 2) {
            return true;
        }

        Set<String> firstTokens = tokens(firstName);
        Set<String> secondTokens = tokens(secondName);
        Set<String> intersection = new LinkedHashSet<>(firstTokens);
        intersection.retainAll(secondTokens);
        Set<String> union = new LinkedHashSet<>(firstTokens);
        union.addAll(secondTokens);

        return intersection.size() >= 2
                && !union.isEmpty()
                && (double) intersection.size() / union.size() >= 0.6;
    }

    private boolean hasSameMeasure(VisionOffer first, VisionOffer second) {
        return first.quantidade() != null
                && second.quantidade() != null
                && first.quantidade().compareTo(second.quantidade()) == 0
                && normalizeText(first.unidade()).equals(normalizeText(second.unidade()));
    }

    private boolean hasEquivalentBrand(String first, String second) {
        String firstBrand = normalizeText(first);
        String secondBrand = normalizeText(second);

        if (firstBrand.isBlank() || secondBrand.isBlank()) {
            return false;
        }

        return firstBrand.equals(secondBrand)
                || firstBrand.contains(secondBrand)
                || secondBrand.contains(firstBrand)
                || levenshteinDistance(firstBrand, secondBrand) <= 1;
    }

    private int levenshteinDistance(String first, String second) {
        int[] previous = new int[second.length() + 1];
        int[] current = new int[second.length() + 1];

        for (int column = 0; column <= second.length(); column++) {
            previous[column] = column;
        }

        for (int row = 1; row <= first.length(); row++) {
            current[0] = row;
            for (int column = 1; column <= second.length(); column++) {
                int replacementCost = first.charAt(row - 1) == second.charAt(column - 1) ? 0 : 1;
                current[column] = Math.min(
                        Math.min(current[column - 1] + 1, previous[column] + 1),
                        previous[column - 1] + replacementCost
                );
            }

            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[second.length()];
    }

    private Set<String> tokens(String value) {
        return Arrays.stream(value.split("\\s+"))
                .filter(token -> token.length() >= 3)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private int sharedTokenCount(String first, String second) {
        Set<String> shared = tokens(first);
        shared.retainAll(tokens(second));
        return shared.size();
    }

    private int score(VisionOffer offer) {
        int score = normalizeText(offer.nome()).length();
        score += offer.marca() == null ? 0 : 5;
        score += offer.quantidade() == null ? 0 : 5;
        score += offer.unidade() == null ? 0 : 5;
        score += offer.textoOriginal() == null ? 0 : normalizeText(offer.textoOriginal()).length();
        score += offer.textoOriginal() != null && offer.textoOriginal().contains("R$") ? 50 : 0;
        return score;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{Alnum}]+", " ")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
