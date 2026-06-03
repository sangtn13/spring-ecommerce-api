package com.ecommerce.sshop.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.ecommerce.sshop.exception.auth.InvalidPasswordResetTokenException;
import com.ecommerce.sshop.exception.user.InvalidUserRequestException;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.user.IUserRepository;
import com.ecommerce.sshop.request.auth.ForgotPasswordRequest;
import com.ecommerce.sshop.request.auth.ResetPasswordRequest;
import com.ecommerce.sshop.service.email.EmailService;
import com.ecommerce.sshop.service.email.EmailTemplateService;
import com.ecommerce.sshop.service.user.IUserService;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private IUserRepository userRepository;
    @Mock
    private IUserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IRefreshTokenService refreshTokenService;
    @Mock
    private RedisTokenService redisTokenService;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "passwordResetTokenExpirationMs", 900000L);
        ReflectionTestUtils.setField(passwordResetService, "resetPasswordBaseUrl", "http://localhost:3000/reset-password");
        ReflectionTestUtils.setField(passwordResetService, "supportEmail", "support@example.com");
    }

    @Test
    @DisplayName("Forgot password sends reset email when user exists")
    void sendResetPasswordEmail_UserExists_SendsEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@gmail.com");

        User user = new User();
        user.setId("user-123");
        user.setEmail("user@gmail.com");
        user.setFirstName("Sang");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(user);
        when(redisTokenService.createPasswordResetToken(user)).thenReturn("token-123");
        when(emailTemplateService.render(
                any(),
                argThat((Map<String, String> placeholders) ->
                        "Sang".equals(placeholders.get("firstName"))
                                && "15".equals(placeholders.get("expiresInMinutes"))
                                && "support@example.com".equals(placeholders.get("supportEmail"))
                                && placeholders.get("resetUrl").contains("token-123"))))
                .thenReturn("<html>reset email</html>");

        passwordResetService.sendResetPasswordEmail(request);

        verify(emailService).sendHtmlEmail("user@gmail.com", "Reset your SShop password", "<html>reset email</html>");
    }

    @Test
    @DisplayName("Forgot password does nothing when user does not exist")
    void sendResetPasswordEmail_UserMissing_DoesNothing() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@gmail.com");

        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(null);

        passwordResetService.sendResetPasswordEmail(request);

        verify(redisTokenService, never()).createPasswordResetToken(any());
        verify(emailTemplateService, never()).render(any(), any());
        verify(emailService, never()).sendHtmlEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Reset password updates password and invalidates existing sessions")
    void resetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("new-password");

        User user = new User();
        user.setId("user-123");
        user.setPassword("encoded-old");

        when(redisTokenService.getPasswordResetUserId("reset-token")).thenReturn("user-123");
        when(userService.getUserById("user-123")).thenReturn(user);
        when(passwordEncoder.matches("new-password", "encoded-old")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");

        passwordResetService.resetPassword(request);

        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(user);
        verify(redisTokenService).deletePasswordResetToken("reset-token");
        verify(redisTokenService).invalidateUserTokens("user-123");
        verify(refreshTokenService).revokeByUserId("user-123");
    }

    @Test
    @DisplayName("Reset password throws when token is invalid")
    void resetPassword_InvalidToken_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("bad-token");
        request.setNewPassword("new-password");

        when(redisTokenService.getPasswordResetUserId("bad-token")).thenReturn(null);

        InvalidPasswordResetTokenException exception = assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> passwordResetService.resetPassword(request));

        assertEquals("Invalid or expired password reset token", exception.getMessage());
        verify(redisTokenService, never()).deletePasswordResetToken(any());
    }

    @Test
    @DisplayName("Reset password throws when new password matches current password")
    void resetPassword_SameAsCurrent_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("new-password");

        User user = new User();
        user.setId("user-123");
        user.setPassword("encoded-old");

        when(redisTokenService.getPasswordResetUserId("reset-token")).thenReturn("user-123");
        when(userService.getUserById("user-123")).thenReturn(user);
        when(passwordEncoder.matches("new-password", "encoded-old")).thenReturn(true);

        InvalidUserRequestException exception = assertThrows(
                InvalidUserRequestException.class,
                () -> passwordResetService.resetPassword(request));

        assertEquals("New password must be different from current password", exception.getMessage());
        verify(redisTokenService, never()).deletePasswordResetToken(any());
    }
}
