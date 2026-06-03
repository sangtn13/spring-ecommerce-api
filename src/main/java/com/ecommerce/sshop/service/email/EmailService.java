package com.ecommerce.sshop.service.email;

public interface EmailService {
    void sendHtmlEmail(String to, String subject, String htmlBody);
}
