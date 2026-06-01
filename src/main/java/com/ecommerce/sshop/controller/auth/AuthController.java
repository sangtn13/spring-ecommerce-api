package com.ecommerce.sshop.controller.auth;

import com.ecommerce.sshop.request.auth.LoginRequest;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.AuthResponse;
import com.ecommerce.sshop.service.auth.IAuthService;
import com.ecommerce.sshop.model.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.authenticate(loginRequest);
            return ResponseEntity.ok(new ApiResponse("Login Successful", authResponse));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(new ApiResponse("User is locked", e.getMessage()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Invalid email or password", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestHeader(name = "X-Refresh-Token", required = false) String refreshToken) {
        try {
            AuthResponse authResponse = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(new ApiResponse("Token refreshed successfully", authResponse));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(new ApiResponse("User is locked", e.getMessage()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Invalid or expired refresh token", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody CreateUserRequest registerRequest) {
        User user = authService.register(registerRequest);
        return ResponseEntity.ok(
                new ApiResponse("Registration successful! You can now login with your credentials.", user.getId()));

    }
}
