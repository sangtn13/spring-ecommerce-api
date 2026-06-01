package com.ecommerce.sshop.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Set;

import com.ecommerce.sshop.model.auth.RefreshToken;
import com.ecommerce.sshop.model.role.Role;
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
    @Mock
    private IRefreshTokenService refreshTokenService;

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
                true, Collections.emptyList());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(jwtUtils.generateTokenForUser(mockAuth)).thenReturn("mocked-jwt-token");
        when(mockAuth.getPrincipal()).thenReturn(userDetails);
        User mockUser = new User();
        mockUser.setId("user-123");
        when(userService.updateLastLogin("user-123")).thenReturn(mockUser);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        when(refreshTokenService.create(mockUser)).thenReturn(refreshToken);

        AuthResponse response = authService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("mocked-jwt-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
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

    @Test
    @DisplayName("Refresh token successfully and rotate refresh token")
    void refreshAccessToken_Success() {
        User user = new User();
        user.setId("user-123");
        user.setEmail("test@gmail.com");
        Role role = new Role();
        role.setName("User");
        user.setRoles(Set.of(role));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("new-refresh-token");

        when(refreshTokenService.verify("old-refresh-token")).thenReturn(user);
        when(jwtUtils.generateTokenForUserEmail("test@gmail.com", "user-123", user.getRoles()))
                .thenReturn("new-access-token");
        when(refreshTokenService.create(user)).thenReturn(refreshToken);

        AuthResponse response = authService.refreshAccessToken("old-refresh-token");

        assertEquals("user-123", response.getId());
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(refreshTokenService).revoke("old-refresh-token");
        verify(userService).updateLastLogin("user-123");
    }
}
