package com.ecommerce.sshop.service.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    private SmtpEmailService smtpEmailService;

    @BeforeEach
    void setUp() {
        smtpEmailService = new SmtpEmailService(javaMailSender);
        ReflectionTestUtils.setField(smtpEmailService, "fromName", "SShop");
        ReflectionTestUtils.setField(smtpEmailService, "fromAddress", "support@example.com");
    }

    @Test
    @DisplayName("Send HTML email populates MIME message and delegates to JavaMailSender")
    void sendHtmlEmail_Success() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpEmailService.sendHtmlEmail("user@gmail.com", "Reset password", "<html>Hello</html>");

        assertEquals("Reset password", mimeMessage.getSubject());
        assertEquals("<html>Hello</html>", mimeMessage.getContent());
        assertEquals("user@gmail.com", ((InternetAddress) mimeMessage.getAllRecipients()[0]).getAddress());
        assertEquals("support@example.com", ((InternetAddress) mimeMessage.getFrom()[0]).getAddress());
        assertEquals("SShop", ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal());
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send HTML email wraps mail exceptions")
    void sendHtmlEmail_MailException_ThrowsIllegalStateException() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        org.mockito.Mockito.doThrow(new MailSendException("smtp down"))
                .when(javaMailSender).send(mimeMessage);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> smtpEmailService.sendHtmlEmail("user@gmail.com", "Reset password", "<html>Hello</html>"));

        assertEquals("Failed to send email", exception.getMessage());
    }
}
