package com.ecommerce.sshop.service.auth;

import com.ecommerce.sshop.model.auth.RefreshToken;
import com.ecommerce.sshop.model.user.User;

public interface IRefreshTokenService {
    RefreshToken create(User user);

    User verify(String token);

    void revoke(String token);

    void revokeByUserId(String userId);
}
