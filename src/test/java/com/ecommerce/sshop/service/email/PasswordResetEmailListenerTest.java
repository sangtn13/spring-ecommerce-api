package com.ecommerce.sshop.service.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordResetEmailListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetEmailListener passwordResetEmailListener;

    @Test
    @DisplayName("Password reset listener delegates email delivery to email service")
    void handlePasswordResetEmailRequested_DelegatesToEmailService() {
        PasswordResetEmailRequestedEvent event = new PasswordResetEmailRequestedEvent(
                "user@gmail.com",
                "Reset your SShop password",
                "<html>reset email</html>");

        passwordResetEmailListener.handlePasswordResetEmailRequested(event);

        verify(emailService).sendHtmlEmail(
                "user@gmail.com",
                "Reset your SShop password",
                "<html>reset email</html>");
    }

    @Test
    @DisplayName("Password reset listener swallows email delivery errors")
    void handlePasswordResetEmailRequested_WhenEmailSendFails_DoesNotThrow() {
        PasswordResetEmailRequestedEvent event = new PasswordResetEmailRequestedEvent(
                "user@gmail.com",
                "Reset your SShop password",
                "<html>reset email</html>");
        doThrow(new IllegalStateException("smtp down"))
                .when(emailService).sendHtmlEmail("user@gmail.com", "Reset your SShop password", "<html>reset email</html>");

        assertDoesNotThrow(() -> passwordResetEmailListener.handlePasswordResetEmailRequested(event));

        verify(emailService).sendHtmlEmail(
                "user@gmail.com",
                "Reset your SShop password",
                "<html>reset email</html>");
    }
}
