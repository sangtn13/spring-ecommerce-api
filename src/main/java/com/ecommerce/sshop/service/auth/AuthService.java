package com.ecommerce.sshop.service.auth;

import com.ecommerce.sshop.exception.auth.InvalidCredentialsException;
import com.ecommerce.sshop.exception.auth.InvalidRefreshTokenException;
import com.ecommerce.sshop.exception.auth.UserLockedAuthException;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.model.auth.RefreshToken;
import com.ecommerce.sshop.request.auth.LoginRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private static final String USER_IS_LOCKED_MESSAGE = "User is locked";

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IUserService userService;
    private final IRefreshTokenService refreshTokenService;

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
}
