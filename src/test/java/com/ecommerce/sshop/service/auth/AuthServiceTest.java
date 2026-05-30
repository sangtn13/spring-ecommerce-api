package com.ecommerce.sshop.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.request.auth.LoginRequest;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.response.AuthResponse;
import com.ecommerce.sshop.security.jwt.JwtUtils;
import com.ecommerce.sshop.security.user.ShopUserDetails;
import com.ecommerce.sshop.service.user.IUserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private IUserService userService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Authenticate successfully and return JWT Token")
    void authenticate_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("password");

        Authentication mockAuth = mock(Authentication.class);
        ShopUserDetails userDetails = new ShopUserDetails("user-123", "test@gmail.com", "pass",
                Collections.emptyList());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(jwtUtils.generateTokenForUser(mockAuth)).thenReturn("mocked-jwt-token");
        when(mockAuth.getPrincipal()).thenReturn(userDetails);

        AuthResponse response = authService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("mocked-jwt-token", response.getToken());
    }

    @Test
    @DisplayName("Register new user successfully through UserService call")
    void register_Success() {
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setEmail("newuser@gmail.com");
        User mockUser = new User();
        mockUser.setId("new-user-id");

        when(userService.createUser(registerRequest)).thenReturn(mockUser);

        User result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("new-user-id", result.getId());
        verify(userService, times(1)).createUser(registerRequest);
    }
}