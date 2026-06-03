package com.ecommerce.sshop.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StringUtilTest {

    @Test
    @DisplayName("trimToNull returns trimmed string when text is present")
    void trimToNull_WithText_ReturnsTrimmedValue() {
        assertEquals("iPhone", StringUtil.trimToNull("  iPhone  "));
    }

    @Test
    @DisplayName("trimToNull returns null for blank input")
    void trimToNull_WithBlankValue_ReturnsNull() {
        assertNull(StringUtil.trimToNull("   "));
    }

    @Test
    @DisplayName("trimToNull returns null for null input")
    void trimToNull_WithNull_ReturnsNull() {
        assertNull(StringUtil.trimToNull(null));
    }
}
