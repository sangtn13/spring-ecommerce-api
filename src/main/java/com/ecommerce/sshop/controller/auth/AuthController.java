package com.ecommerce.sshop.controller.auth;

import com.ecommerce.sshop.request.auth.ChangePasswordRequest;
import com.ecommerce.sshop.request.auth.ForgotPasswordRequest;
import com.ecommerce.sshop.request.auth.LoginRequest;
import com.ecommerce.sshop.request.auth.ResetPasswordRequest;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.AuthResponse;
import com.ecommerce.sshop.service.auth.IAuthService;
import com.ecommerce.sshop.model.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
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
        AuthResponse authResponse = authService.authenticate(loginRequest);
        return ResponseEntity.ok(new ApiResponse("Login Successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestHeader(name = "X-Refresh-Token", required = false) String refreshToken) {
        AuthResponse authResponse = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new ApiResponse("Token refreshed successfully", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody CreateUserRequest registerRequest) {
        User user = authService.register(registerRequest);
        return ResponseEntity.ok(
                new ApiResponse("Registration successful! You can now login with your credentials.", user.getId()));

    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        authService.logout(authorizationHeader);
        return ResponseEntity.ok(new ApiResponse("Logout successful", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authorizationHeader, request);
        return ResponseEntity.ok(new ApiResponse("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new ApiResponse(
                "If an account exists for that email, a password reset email has been sent.",
                null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse("Password reset successfully", null));
    }
}
