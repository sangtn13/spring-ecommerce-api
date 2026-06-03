package com.ecommerce.sshop.util;

import org.springframework.util.StringUtils;

public final class StringUtil {
    private StringUtil() {
    }

    public static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
