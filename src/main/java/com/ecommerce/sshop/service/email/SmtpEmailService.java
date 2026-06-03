package com.ecommerce.sshop.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${sshop.mail.fromName}")
    private String fromName;

    @Value("${sshop.mail.fromAddress}")
    private String fromAddress;

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(fromAddress, fromName);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException exception) {
            throw new IllegalStateException("Failed to send email", exception);
        }
    }
}
