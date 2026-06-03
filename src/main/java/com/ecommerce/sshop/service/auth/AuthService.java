package com.ecommerce.sshop.service.auth;

import com.ecommerce.sshop.exception.auth.InvalidCredentialsException;
import com.ecommerce.sshop.exception.auth.InvalidRefreshTokenException;
import com.ecommerce.sshop.exception.auth.UserLockedAuthException;
import com.ecommerce.sshop.exception.user.InvalidUserRequestException;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.model.auth.RefreshToken;
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

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private static final String USER_IS_LOCKED_MESSAGE = "User is locked";

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IUserService userService;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IRefreshTokenService refreshTokenService;
    private final RedisTokenService redisTokenService;
    private final PasswordResetService passwordResetService;

    @Override
    public AuthResponse authenticate(LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (LockedException exception) {
            throw new UserLockedAuthException(USER_IS_LOCKED_MESSAGE);
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateTokenForUser(authentication);
        ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();
        User latestUser = userService.updateLastLogin(userDetails.getId());
        if (Boolean.TRUE.equals(latestUser.getAccountLocked())) {
            throw new UserLockedAuthException(USER_IS_LOCKED_MESSAGE);
        }
        RefreshToken refreshToken = refreshTokenService.create(latestUser);
        return new AuthResponse(userDetails.getId(), jwt, refreshToken.getToken());
    }

    @Override
    public AuthResponse refreshAccessToken(String oldRefreshToken) {
        if (oldRefreshToken == null || oldRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token not found");
        }

        User user;
        try {
            user = refreshTokenService.verify(oldRefreshToken);
        } catch (LockedException exception) {
            throw new UserLockedAuthException(USER_IS_LOCKED_MESSAGE);
        }
        if (user == null) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }

        refreshTokenService.revoke(oldRefreshToken);
        userService.updateLastLogin(user.getId());
        String accessToken = jwtUtils.generateTokenForUserEmail(user.getEmail(), user.getId(), user.getRoles());
        RefreshToken newRefreshToken = refreshTokenService.create(user);
        return new AuthResponse(user.getId(), accessToken, newRefreshToken.getToken());
    }

    @Override
    public User register(CreateUserRequest registerRequest) {
        return userService.createUser(registerRequest);
    }

    @Override
    public void logout(String authorizationHeader) {
        User currentUser = userService.getCurrentUser();
        redisTokenService.blacklistAccessToken(extractAccessToken(authorizationHeader));
        refreshTokenService.revokeByUserId(currentUser.getId());
        SecurityContextHolder.clearContext();
    }

    @Override
    public void changePassword(String authorizationHeader, ChangePasswordRequest request) {
        User currentUser = userService.getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        updatePassword(currentUser, request.getNewPassword());
        redisTokenService.invalidateUserTokens(currentUser.getId());
        redisTokenService.blacklistAccessToken(extractAccessToken(authorizationHeader));
        refreshTokenService.revokeByUserId(currentUser.getId());
        SecurityContextHolder.clearContext();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        passwordResetService.sendResetPasswordEmail(request);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }

    private String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException("Access token not found");
        }

        String accessToken = authorizationHeader.substring(7).trim();
        if (accessToken.isBlank()) {
            throw new InvalidCredentialsException("Access token not found");
        }
        return accessToken;
    }

    private void updatePassword(User user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new InvalidUserRequestException("New password must be different from current password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
