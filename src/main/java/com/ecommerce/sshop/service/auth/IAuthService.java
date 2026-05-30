package com.ecommerce.sshop.service.auth;

import com.ecommerce.sshop.request.auth.LoginRequest;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.response.AuthResponse;
import com.ecommerce.sshop.model.user.User;

public interface IAuthService {
    AuthResponse authenticate(LoginRequest loginRequest);

    User register(CreateUserRequest registerRequest);
}
