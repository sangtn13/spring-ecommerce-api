package com.ecommerce.sshop.service.email;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class EmailTemplateServiceTest {

    private final EmailTemplateService emailTemplateService = new EmailTemplateService();

    @Test
    void render_ReplacesAllPlaceholders() {
        String html = emailTemplateService.render(
                "templates/email/password-reset.html",
                Map.of(
                        "firstName", "Sang",
                        "resetUrl", "http://localhost/reset?token=abc",
                        "expiresInMinutes", "15",
                        "supportEmail", "support@example.com"));

        assertTrue(html.contains("Sang"));
        assertTrue(html.contains("http://localhost/reset?token=abc"));
        assertTrue(html.contains("15 minutes"));
        assertTrue(html.contains("support@example.com"));
        assertFalse(html.contains("{{firstName}}"));
        assertFalse(html.contains("{{resetUrl}}"));
    }
}
