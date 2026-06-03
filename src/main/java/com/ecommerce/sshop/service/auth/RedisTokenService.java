package com.ecommerce.sshop.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.security.jwt.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisTokenService {
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String PASSWORD_RESET_PREFIX = "auth:password-reset:";
    private static final String USER_TOKEN_INVALID_AFTER_PREFIX = "auth:user-token-invalid-after:";

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtils jwtUtils;

    @Value("${sshop.app.jwtExpirationMs}")
    private long jwtExpirationMs;

    @Value("${sshop.app.passwordResetTokenExpirationMs:900000}")
    private long passwordResetTokenExpirationMs;

    public void blacklistAccessToken(String token) {
        Duration ttl = Duration.between(Instant.now(), jwtUtils.getExpirationFromJwtToken(token).toInstant());
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        stringRedisTemplate.opsForValue().set(accessTokenBlacklistKey(token), "1", ttl);
    }

    public boolean isAccessTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(accessTokenBlacklistKey(token)));
    }

    public void invalidateUserTokens(String userId) {
        stringRedisTemplate.opsForValue().set(
                userTokenInvalidAfterKey(userId),
                String.valueOf(Instant.now().toEpochMilli()),
                Duration.ofMillis(jwtExpirationMs));
    }

    public boolean wasIssuedBeforeUserInvalidation(String userId, Instant issuedAt) {
        String invalidAfter = stringRedisTemplate.opsForValue().get(userTokenInvalidAfterKey(userId));
        if (invalidAfter == null || invalidAfter.isBlank()) {
            return false;
        }

        return issuedAt.toEpochMilli() < Long.parseLong(invalidAfter);
    }

    public String createPasswordResetToken(User user) {
        String token = UUID.randomUUID() + "." + UUID.randomUUID();
        stringRedisTemplate.opsForValue().set(
                passwordResetKey(token),
                user.getId(),
                Duration.ofMillis(passwordResetTokenExpirationMs));
        return token;
    }

    public String getPasswordResetUserId(String token) {
        return stringRedisTemplate.opsForValue().get(passwordResetKey(token));
    }

    public void deletePasswordResetToken(String token) {
        stringRedisTemplate.delete(passwordResetKey(token));
    }

    private String accessTokenBlacklistKey(String token) {
        return ACCESS_TOKEN_BLACKLIST_PREFIX + sha256(token);
    }

    private String passwordResetKey(String token) {
        return PASSWORD_RESET_PREFIX + token;
    }

    private String userTokenInvalidAfterKey(String userId) {
        return USER_TOKEN_INVALID_AFTER_PREFIX + userId;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
