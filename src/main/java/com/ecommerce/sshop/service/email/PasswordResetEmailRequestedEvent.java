package com.ecommerce.sshop.service.email;

public record PasswordResetEmailRequestedEvent(String to, String subject, String htmlBody) {
}
