package com.ecommerce.sshop.service.auth;

import com.ecommerce.sshop.request.auth.ChangePasswordRequest;
import com.ecommerce.sshop.request.auth.ForgotPasswordRequest;
import com.ecommerce.sshop.request.auth.LoginRequest;
import com.ecommerce.sshop.request.auth.ResetPasswordRequest;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.response.AuthResponse;
import com.ecommerce.sshop.model.user.User;

public interface IAuthService {
    AuthResponse authenticate(LoginRequest loginRequest);

    AuthResponse refreshAccessToken(String oldRefreshToken);

    User register(CreateUserRequest registerRequest);

    void logout(String authorizationHeader);

    void changePassword(String authorizationHeader, ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
