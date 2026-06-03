package com.ecommerce.sshop.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductSearchUtilTest {

    @Test
    @DisplayName("normalizePhrase removes punctuation, trims, and lowercases")
    void normalizePhrase_NormalizesText() {
        assertEquals("vertex 14 pro", ProductSearchUtil.normalizePhrase("  Vertex-14, Pro! "));
    }

    @Test
    @DisplayName("tokenize returns normalized tokens")
    void tokenize_ReturnsTokens() {
        assertEquals(List.of("vertex", "14", "pro"), ProductSearchUtil.tokenize("Vertex-14 Pro"));
    }

    @Test
    @DisplayName("buildBooleanModeKeyword keeps unique terms that meet minimum length")
    void buildBooleanModeKeyword_BuildsQuery() {
        assertEquals("+vertex* +pro*", ProductSearchUtil.buildBooleanModeKeyword("vertex pro vertex", 3));
    }

    @Test
    @DisplayName("buildNameSearchFilter falls back to phrase when terms are too short")
    void buildNameSearchFilter_UsesPhraseFallback() {
        ProductSearchUtil.NameSearchFilter filter = ProductSearchUtil.buildNameSearchFilter("14", 3);

        assertNull(filter.fullTextQuery());
        assertEquals("14", filter.normalizedPhrase());
    }
}
