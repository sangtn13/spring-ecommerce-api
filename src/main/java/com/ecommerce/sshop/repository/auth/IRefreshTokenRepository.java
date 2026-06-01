package com.ecommerce.sshop.repository.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sshop.model.auth.RefreshToken;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserId(String userId);
}
