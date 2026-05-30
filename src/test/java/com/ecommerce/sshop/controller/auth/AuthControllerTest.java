package com.ecommerce.sshop.controller.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.springframework.security.core.AuthenticationException;

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
        AuthResponse mockAuthResponse = new AuthResponse("user-uuid-123", "mocked-jwt-token-string");
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
    @DisplayName("Authenticate failed due to invalid credentials - Return HTTP 401 Unauthorized")
    void login_Failure_InvalidCredentials() {
        // Given
        // Tạo một instance kế thừa từ lớp trừu tượng AuthenticationException để làm Mock gián tiếp
        AuthenticationException mockException = new AuthenticationException("Bad credentials") {};
        when(authService.authenticate(loginRequest)).thenThrow(mockException);

        // When
        ResponseEntity<ApiResponse> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid email or password", response.getBody().getMessage());
        assertEquals("Bad credentials", response.getBody().getData());
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