package com.ecommerce.sshop.controller.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ecommerce.sshop.exception.auth.InvalidCredentialsException;
import com.ecommerce.sshop.exception.auth.InvalidRefreshTokenException;
import com.ecommerce.sshop.exception.auth.UserLockedAuthException;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.request.auth.LoginRequest;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.AuthResponse;
import com.ecommerce.sshop.service.auth.IAuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private IAuthService authService;

    @InjectMocks private AuthController authController;

    private LoginRequest loginRequest;
    private CreateUserRequest registerRequest;

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
    }

    @Test
    @DisplayName("Authenticate successfully and return JWT Token")
    void login_Success() {
        // Given
        AuthResponse mockAuthResponse = new AuthResponse("user-uuid-123", "mocked-jwt-token-string", "refresh-token");
        when(authService.authenticate(loginRequest)).thenReturn(mockAuthResponse);

        // When
        ResponseEntity<ApiResponse> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login Successful", response.getBody().getMessage());
        assertEquals(mockAuthResponse, response.getBody().getData());
        verify(authService, times(1)).authenticate(loginRequest);
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
    @DisplayName("Authenticate failed due to invalid credentials - Return HTTP 401 Unauthorized")
    void login_Failure_InvalidCredentials() {
        when(authService.authenticate(loginRequest)).thenThrow(new InvalidCredentialsException("Invalid email or password"));

        assertThrows(InvalidCredentialsException.class, () -> authController.login(loginRequest));
        verify(authService, times(1)).authenticate(loginRequest);
    }

    @Test
    @DisplayName("Register new user successfully - Return HTTP 200 OK and User ID")
    void register_Success() {
        // Given
        User mockUser = new User();
        mockUser.setId("new-user-uuid");
        when(authService.register(registerRequest)).thenReturn(mockUser);

        // When
        ResponseEntity<ApiResponse> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Registration successful! You can now login with your credentials.", response.getBody().getMessage());
        assertEquals("new-user-uuid", response.getBody().getData());
        verify(authService, times(1)).register(registerRequest);
    }
}
