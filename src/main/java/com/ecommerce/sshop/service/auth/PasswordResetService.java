package com.ecommerce.sshop.service.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.sshop.exception.auth.InvalidPasswordResetTokenException;
import com.ecommerce.sshop.exception.auth.PasswordResetEmailCooldownException;
import com.ecommerce.sshop.exception.auth.PasswordResetIpRateLimitException;
import com.ecommerce.sshop.exception.user.InvalidUserRequestException;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.user.IUserRepository;
import com.ecommerce.sshop.request.auth.ForgotPasswordRequest;
import com.ecommerce.sshop.request.auth.ResetPasswordRequest;
import com.ecommerce.sshop.service.email.EmailTemplateService;
import com.ecommerce.sshop.service.email.PasswordResetEmailRequestedEvent;
import com.ecommerce.sshop.service.user.IUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final String PASSWORD_RESET_IP_RATE_LIMIT_MESSAGE =
            "Too many password reset requests from this IP. Please try again later.";

    private final IUserRepository userRepository;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final IRefreshTokenService refreshTokenService;
    private final RedisTokenService redisTokenService;
    private final EmailTemplateService emailTemplateService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${sshop.app.passwordResetTokenExpirationMs:900000}")
    private long passwordResetTokenExpirationMs;

    @Value("${sshop.app.passwordResetEmailCooldownMs:120000}")
    private long passwordResetEmailCooldownMs;

    @Value("${sshop.app.resetPasswordBaseUrl}")
    private String resetPasswordBaseUrl;

    @Value("${sshop.mail.supportEmail}")
    private String supportEmail;

    public void sendResetPasswordEmail(ForgotPasswordRequest request, String clientIp) {
        if (redisTokenService.isPasswordResetIpRateLimitExceeded(clientIp)) {
            throw new PasswordResetIpRateLimitException(PASSWORD_RESET_IP_RATE_LIMIT_MESSAGE);
        }

        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            return;
        }

        if (!redisTokenService.tryAcquirePasswordResetEmailCooldown(user.getEmail())) {
            throw new PasswordResetEmailCooldownException(buildPasswordResetEmailCooldownMessage());
        }

        String token = redisTokenService.createPasswordResetToken(user);
        String resetUrl = buildResetUrl(token);
        String firstName = user.getFirstName() == null || user.getFirstName().isBlank()
                ? "there"
                : user.getFirstName();
        long expiresInMinutes = Duration.ofMillis(passwordResetTokenExpirationMs).toMinutes();

        String html = emailTemplateService.render(
                "templates/email/password-reset.html",
                Map.of(
                        "firstName", firstName,
                        "resetUrl", resetUrl,
                        "expiresInMinutes", String.valueOf(expiresInMinutes),
                        "supportEmail", supportEmail));

        applicationEventPublisher.publishEvent(
                new PasswordResetEmailRequestedEvent(user.getEmail(), "Reset your SShop password", html));
    }

    public void resetPassword(ResetPasswordRequest request) {
        String userId = redisTokenService.getPasswordResetUserId(request.getToken());
        if (userId == null || userId.isBlank()) {
            throw new InvalidPasswordResetTokenException("Invalid or expired password reset token");
        }

        User user = userService.getUserById(userId);
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidUserRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisTokenService.deletePasswordResetToken(request.getToken());
        redisTokenService.invalidateUserTokens(user.getId());
        refreshTokenService.revokeByUserId(user.getId());
    }

    private String buildResetUrl(String token) {
        return resetPasswordBaseUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String buildPasswordResetEmailCooldownMessage() {
        long cooldownSeconds = Math.max(1L, Duration.ofMillis(passwordResetEmailCooldownMs).toSeconds());
        return "Please wait " + cooldownSeconds + " seconds before requesting another password reset email.";
    }
}
