package com.ecommerce.sshop.service.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.sshop.exception.auth.InvalidPasswordResetTokenException;
import com.ecommerce.sshop.exception.user.InvalidUserRequestException;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.user.IUserRepository;
import com.ecommerce.sshop.request.auth.ForgotPasswordRequest;
import com.ecommerce.sshop.request.auth.ResetPasswordRequest;
import com.ecommerce.sshop.service.email.EmailService;
import com.ecommerce.sshop.service.email.EmailTemplateService;
import com.ecommerce.sshop.service.user.IUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final IUserRepository userRepository;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final IRefreshTokenService refreshTokenService;
    private final RedisTokenService redisTokenService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${sshop.app.passwordResetTokenExpirationMs:900000}")
    private long passwordResetTokenExpirationMs;

    @Value("${sshop.app.resetPasswordBaseUrl}")
    private String resetPasswordBaseUrl;

    @Value("${sshop.mail.supportEmail}")
    private String supportEmail;

    public void sendResetPasswordEmail(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            return;
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

        emailService.sendHtmlEmail(user.getEmail(), "Reset your SShop password", html);
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
}
