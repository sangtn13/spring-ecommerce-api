package com.ecommerce.sshop.controller.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ecommerce.sshop.exception.auth.InvalidCredentialsException;
import com.ecommerce.sshop.exception.auth.InvalidPasswordResetTokenException;
import com.ecommerce.sshop.exception.auth.InvalidRefreshTokenException;
import com.ecommerce.sshop.exception.auth.UserLockedAuthException;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.request.auth.ChangePasswordRequest;
import com.ecommerce.sshop.request.auth.ForgotPasswordRequest;
import com.ecommerce.sshop.request.auth.LoginRequest;
import com.ecommerce.sshop.request.auth.ResetPasswordRequest;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.AuthResponse;
import com.ecommerce.sshop.service.auth.IAuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private IAuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private CreateUserRequest registerRequest;
    private ChangePasswordRequest changePasswordRequest;
    private ForgotPasswordRequest forgotPasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("sangtn@gmail.com");
        loginRequest.setPassword("password123");

        registerRequest = new CreateUserRequest();
        registerRequest.setEmail("newuser@gmail.com");
        registerRequest.setPassword("securepass");
        registerRequest.setFirstName("Sang");
        registerRequest.setLastName("Tran");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("old-password");
        changePasswordRequest.setNewPassword("new-password");

        forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail("sangtn@gmail.com");

        resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setToken("reset-token");
        resetPasswordRequest.setNewPassword("new-password");
    }

    @Test
    @DisplayName("Authenticate successfully and return JWT token pair")
    void login_Success() {
        AuthResponse mockAuthResponse = new AuthResponse("user-uuid-123", "mocked-jwt-token-string", "refresh-token");
        when(authService.authenticate(loginRequest)).thenReturn(mockAuthResponse);

        ResponseEntity<ApiResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login Successful", response.getBody().getMessage());
        assertEquals(mockAuthResponse, response.getBody().getData());
        verify(authService).authenticate(loginRequest);
    }

    @Test
    @DisplayName("Refresh token successfully")
    void refreshToken_Success() {
        AuthResponse mockAuthResponse = new AuthResponse("user-uuid-123", "new-access", "new-refresh");
        when(authService.refreshAccessToken("old-refresh")).thenReturn(mockAuthResponse);

        ResponseEntity<ApiResponse> response = authController.refreshToken("old-refresh");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Token refreshed successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Register new user successfully")
    void register_Success() {
        User mockUser = new User();
        mockUser.setId("new-user-uuid");
        when(authService.register(registerRequest)).thenReturn(mockUser);

        ResponseEntity<ApiResponse> response = authController.register(registerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Registration successful! You can now login with your credentials.", response.getBody().getMessage());
        assertEquals("new-user-uuid", response.getBody().getData());
        verify(authService).register(registerRequest);
    }

    @Test
    @DisplayName("Logout succeeds")
    void logout_Success() {
        ResponseEntity<ApiResponse> response = authController.logout("Bearer access-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody().getMessage());
        verify(authService).logout("Bearer access-token");
    }

    @Test
    @DisplayName("Change password succeeds")
    void changePassword_Success() {
        ResponseEntity<ApiResponse> response = authController.changePassword("Bearer access-token", changePasswordRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed successfully", response.getBody().getMessage());
        verify(authService).changePassword("Bearer access-token", changePasswordRequest);
    }

    @Test
    @DisplayName("Forgot password succeeds")
    void forgotPassword_Success() {
        ResponseEntity<ApiResponse> response = authController.forgotPassword(forgotPasswordRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("If an account exists for that email, a password reset email has been sent.", response.getBody().getMessage());
        assertEquals(null, response.getBody().getData());
        verify(authService).forgotPassword(forgotPasswordRequest);
    }

    @Test
    @DisplayName("Reset password succeeds")
    void resetPassword_Success() {
        ResponseEntity<ApiResponse> response = authController.resetPassword(resetPasswordRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully", response.getBody().getMessage());
        verify(authService).resetPassword(resetPasswordRequest);
    }

    @Test
    @DisplayName("Login failed due to locked account")
    void login_Failure_Locked() {
        when(authService.authenticate(loginRequest)).thenThrow(new UserLockedAuthException("User is locked"));
        assertThrows(UserLockedAuthException.class, () -> authController.login(loginRequest));
    }

    @Test
    void refreshToken_Invalid() {
        when(authService.refreshAccessToken("bad")).thenThrow(new InvalidRefreshTokenException("Invalid or expired refresh token"));
        assertThrows(InvalidRefreshTokenException.class, () -> authController.refreshToken("bad"));
    }

    @Test
    void refreshToken_Locked() {
        when(authService.refreshAccessToken("locked-token")).thenThrow(new UserLockedAuthException("User is locked"));
        assertThrows(UserLockedAuthException.class, () -> authController.refreshToken("locked-token"));
    }

    @Test
    @DisplayName("Login failed due to invalid credentials")
    void login_Failure_InvalidCredentials() {
        when(authService.authenticate(loginRequest)).thenThrow(new InvalidCredentialsException("Invalid email or password"));

        assertThrows(InvalidCredentialsException.class, () -> authController.login(loginRequest));
        verify(authService, times(1)).authenticate(loginRequest);
    }

    @Test
    @DisplayName("Change password bubbles invalid credential error")
    void changePassword_InvalidCurrentPassword() {
        doThrow(new InvalidCredentialsException("Current password is incorrect"))
                .when(authService).changePassword("Bearer access-token", changePasswordRequest);

        assertThrows(InvalidCredentialsException.class,
                () -> authController.changePassword("Bearer access-token", changePasswordRequest));
    }

    @Test
    @DisplayName("Reset password bubbles invalid token error")
    void resetPassword_InvalidToken() {
        doThrow(new InvalidPasswordResetTokenException("Invalid or expired password reset token"))
                .when(authService).resetPassword(resetPasswordRequest);

        assertThrows(InvalidPasswordResetTokenException.class,
                () -> authController.resetPassword(resetPasswordRequest));
    }
}
