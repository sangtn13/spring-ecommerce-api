package com.ecommerce.sshop.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.sshop.exception.auth.InvalidCredentialsException;
import com.ecommerce.sshop.exception.auth.InvalidRefreshTokenException;
import com.ecommerce.sshop.exception.auth.UserLockedAuthException;
import com.ecommerce.sshop.exception.user.InvalidUserRequestException;
import com.ecommerce.sshop.model.auth.RefreshToken;
import com.ecommerce.sshop.model.role.Role;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.user.IUserRepository;
import com.ecommerce.sshop.request.auth.ChangePasswordRequest;
import com.ecommerce.sshop.request.auth.ForgotPasswordRequest;
import com.ecommerce.sshop.request.auth.LoginRequest;
import com.ecommerce.sshop.request.auth.ResetPasswordRequest;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.response.AuthResponse;
import com.ecommerce.sshop.security.jwt.JwtUtils;
import com.ecommerce.sshop.security.user.ShopUserDetails;
import com.ecommerce.sshop.service.user.IUserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private IUserService userService;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IRefreshTokenService refreshTokenService;
    @Mock
    private RedisTokenService redisTokenService;
    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Authenticate successfully and return JWT token pair")
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
        verify(userService).createUser(registerRequest);
    }

    @Test
    @DisplayName("Refresh token successfully rotates token pair")
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

    @Test
    @DisplayName("Logout blacklists access token and revokes refresh token")
    void logout_Success() {
        User currentUser = new User();
        currentUser.setId("user-123");
        when(userService.getCurrentUser()).thenReturn(currentUser);

        authService.logout("Bearer access-token");

        verify(redisTokenService).blacklistAccessToken("access-token");
        verify(refreshTokenService).revokeByUserId("user-123");
    }

    @Test
    @DisplayName("Change password updates password and invalidates current sessions")
    void changePassword_Success() {
        User currentUser = new User();
        currentUser.setId("user-123");
        currentUser.setPassword("encoded-old");
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "encoded-old")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-password");
        request.setNewPassword("new-password");

        authService.changePassword("Bearer access-token", request);

        assertEquals("encoded-new", currentUser.getPassword());
        verify(userRepository).save(currentUser);
        verify(redisTokenService).invalidateUserTokens("user-123");
        verify(redisTokenService).blacklistAccessToken("access-token");
        verify(refreshTokenService).revokeByUserId("user-123");
    }

    @Test
    @DisplayName("Forgot password delegates to password reset service")
    void forgotPassword_DelegatesToPasswordResetService() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@gmail.com");

        authService.forgotPassword(request, "127.0.0.1");

        verify(passwordResetService).sendResetPasswordEmail(request, "127.0.0.1");
    }

    @Test
    @DisplayName("Reset password delegates to password reset service")
    void resetPassword_DelegatesToPasswordResetService() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("new-password");

        authService.resetPassword(request);

        verify(passwordResetService).resetPassword(request);
    }

    @Test
    @DisplayName("Authenticate throws locked exception when account is locked by Spring Security")
    void authenticate_LockedByAuthenticationManager_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("locked@gmail.com");
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("locked"));

        UserLockedAuthException exception = assertThrows(UserLockedAuthException.class,
                () -> authService.authenticate(loginRequest));

        assertEquals("User is locked", exception.getMessage());
    }

    @Test
    @DisplayName("Authenticate throws invalid credentials when authentication fails")
    void authenticate_InvalidCredentials_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrong@gmail.com");
        loginRequest.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.authenticate(loginRequest));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    @DisplayName("Authenticate throws locked exception when user is locked after login refresh")
    void authenticate_UserLockedAfterLogin_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("password");

        Authentication mockAuth = mock(Authentication.class);
        ShopUserDetails userDetails = new ShopUserDetails("user-123", "test@gmail.com", "pass",
                true, Collections.emptyList());
        User lockedUser = new User();
        lockedUser.setId("user-123");
        lockedUser.setAccountLocked(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(jwtUtils.generateTokenForUser(mockAuth)).thenReturn("mocked-jwt-token");
        when(mockAuth.getPrincipal()).thenReturn(userDetails);
        when(userService.updateLastLogin("user-123")).thenReturn(lockedUser);

        UserLockedAuthException exception = assertThrows(UserLockedAuthException.class,
                () -> authService.authenticate(loginRequest));

        assertEquals("User is locked", exception.getMessage());
        verify(refreshTokenService, never()).create(any());
    }

    @Test
    @DisplayName("Refresh token throws invalid refresh token when input is blank")
    void refreshAccessToken_BlankToken_ThrowsException() {
        InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refreshAccessToken(" "));

        assertEquals("Refresh token not found", exception.getMessage());
    }

    @Test
    @DisplayName("Refresh token throws locked exception when verified user is locked")
    void refreshAccessToken_Locked_ThrowsException() {
        when(refreshTokenService.verify("locked-token")).thenThrow(new LockedException("locked"));

        UserLockedAuthException exception = assertThrows(UserLockedAuthException.class,
                () -> authService.refreshAccessToken("locked-token"));

        assertEquals("User is locked", exception.getMessage());
    }

    @Test
    @DisplayName("Refresh token throws invalid refresh token when verification returns null")
    void refreshAccessToken_InvalidOrExpired_ThrowsException() {
        when(refreshTokenService.verify("expired-token")).thenReturn(null);

        InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refreshAccessToken("expired-token"));

        assertEquals("Invalid or expired refresh token", exception.getMessage());
        verify(refreshTokenService, never()).revoke(any(String.class));
    }

    @Test
    @DisplayName("Logout throws when authorization header is missing")
    void logout_MissingAuthorization_ThrowsException() {
        User currentUser = new User();
        currentUser.setId("user-123");
        when(userService.getCurrentUser()).thenReturn(currentUser);

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.logout(null));

        assertEquals("Access token not found", exception.getMessage());
        verify(refreshTokenService, never()).revokeByUserId(any());
    }

    @Test
    @DisplayName("Change password throws when current password is incorrect")
    void changePassword_InvalidCurrentPassword_ThrowsException() {
        User currentUser = new User();
        currentUser.setPassword("encoded-old");
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(passwordEncoder.matches("wrong-password", "encoded-old")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong-password");
        request.setNewPassword("new-password");

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.changePassword("Bearer access-token", request));

        assertEquals("Current password is incorrect", exception.getMessage());
    }

    @Test
    @DisplayName("Change password throws when new password matches current password")
    void changePassword_SameAsCurrent_ThrowsException() {
        User currentUser = new User();
        currentUser.setPassword("encoded-old");
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);
        when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-password");
        request.setNewPassword("old-password");

        InvalidUserRequestException exception = assertThrows(InvalidUserRequestException.class,
                () -> authService.changePassword("Bearer access-token", request));

        assertEquals("New password must be different from current password", exception.getMessage());
    }

    @Test
    @DisplayName("Reset password bubbles invalid token from password reset service")
    void resetPassword_InvalidToken_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("bad-token");
        request.setNewPassword("new-password");

        doThrow(new com.ecommerce.sshop.exception.auth.InvalidPasswordResetTokenException(
                "Invalid or expired password reset token"))
                .when(passwordResetService).resetPassword(request);

        com.ecommerce.sshop.exception.auth.InvalidPasswordResetTokenException exception =
                assertThrows(com.ecommerce.sshop.exception.auth.InvalidPasswordResetTokenException.class,
                () -> authService.resetPassword(request));

        assertEquals("Invalid or expired password reset token", exception.getMessage());
    }

    @Test
    @DisplayName("Reset password bubbles invalid user request from password reset service")
    void resetPassword_SameAsCurrent_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("new-password");

        doThrow(new InvalidUserRequestException("New password must be different from current password"))
                .when(passwordResetService).resetPassword(request);

        InvalidUserRequestException exception = assertThrows(InvalidUserRequestException.class,
                () -> authService.resetPassword(request));

        assertEquals("New password must be different from current password", exception.getMessage());
    }
}
