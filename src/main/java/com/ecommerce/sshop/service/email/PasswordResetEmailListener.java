package com.ecommerce.sshop.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetEmailListener {
    private final EmailService emailService;

    @Async("emailTaskExecutor")
    @TransactionalEventListener(fallbackExecution = true)
    public void handlePasswordResetEmailRequested(PasswordResetEmailRequestedEvent event) {
        try {
            emailService.sendHtmlEmail(event.to(), event.subject(), event.htmlBody());
        } catch (RuntimeException exception) {
            log.error("Failed to send password reset email to {}", event.to(), exception);
        }
    }
}
