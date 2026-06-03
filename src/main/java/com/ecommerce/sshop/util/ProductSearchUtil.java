package com.ecommerce.sshop.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ProductSearchUtil {
    private ProductSearchUtil() {
    }

    public static NameSearchFilter buildNameSearchFilter(String value, int minFullTextTermLength) {
        String trimmedValue = StringUtil.trimToNull(value);
        if (trimmedValue == null) {
            return new NameSearchFilter(null, null);
        }

        List<String> tokens = tokenize(trimmedValue);
        boolean hasShortToken = tokens.stream().anyMatch(term -> term.length() < minFullTextTermLength);
        String fullTextQuery = buildBooleanModeKeyword(trimmedValue, minFullTextTermLength);
        String normalizedPhrase = hasShortToken ? normalizePhrase(trimmedValue) : null;

        if (hasShortToken && StringUtil.trimToNull(fullTextQuery) == null) {
            return new NameSearchFilter(null, normalizedPhrase);
        }

        return new NameSearchFilter(fullTextQuery, normalizedPhrase);
    }

    public static String buildBooleanModeKeyword(String keyword, int minFullTextTermLength) {
        Set<String> terms = tokenize(keyword).stream()
                .filter(term -> term.length() >= minFullTextTermLength)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (terms.isEmpty()) {
            return null;
        }

        return terms.stream()
                .map(term -> "+" + term + "*")
                .collect(Collectors.joining(" "));
    }

    public static List<String> tokenize(String value) {
        String normalized = normalizePhrase(value);
        if (normalized == null || normalized.isBlank()) {
            return List.of();
        }
        return new ArrayList<>(Arrays.asList(normalized.split(" ")));
    }

    public static String normalizePhrase(String value) {
        String trimmedValue = StringUtil.trimToNull(value);
        if (trimmedValue == null) {
            return null;
        }

        return trimmedValue.replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    public record NameSearchFilter(String fullTextQuery, String normalizedPhrase) {
    }
}
