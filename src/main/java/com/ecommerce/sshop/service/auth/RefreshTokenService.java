package com.ecommerce.sshop.service.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sshop.model.auth.RefreshToken;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.auth.IRefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {
    private final IRefreshTokenRepository refreshTokenRepository;

    @Value("${sshop.app.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    @Override
    public RefreshToken create(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId()).orElseGet(RefreshToken::new);
        refreshToken.setUser(user);
        refreshToken.setToken(generateToken());
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));
        refreshToken.setRevokedAt(null);
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public User verify(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElse(null);
        if (refreshToken == null) {
            return null;
        }
        if (refreshToken.getRevokedAt() != null) {
            return null;
        }
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        User user = refreshToken.getUser();
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new LockedException("User is locked.");
        }
        return user;
    }

    @Override
    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            if (refreshToken.getRevokedAt() == null) {
                refreshToken.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(refreshToken);
            }
        });
    }

    private String generateToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }
}
