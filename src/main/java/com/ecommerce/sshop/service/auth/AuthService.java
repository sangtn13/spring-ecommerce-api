package com.ecommerce.sshop.service.auth;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.request.auth.LoginRequest;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.response.AuthResponse;
import com.ecommerce.sshop.security.jwt.JwtUtils;
import com.ecommerce.sshop.security.user.ShopUserDetails;
import com.ecommerce.sshop.service.user.IUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IUserService userService;

    @Override
    public AuthResponse authenticate(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateTokenForUser(authentication);
        ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();
        return new AuthResponse(userDetails.getId(), jwt);
    }

    @Override
    public User register(CreateUserRequest registerRequest) {
        return userService.createUser(registerRequest);
    }
}
