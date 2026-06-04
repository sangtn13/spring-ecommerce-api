package com.ecommerce.sshop.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.security.jwt.JwtUtils;

@ExtendWith(MockitoExtension.class)
class RedisTokenServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisTokenService redisTokenService;
    private static final Duration IP_RATE_LIMIT_WINDOW = Duration.ofMillis(900000L);
    private static final Duration PASSWORD_RESET_TTL = Duration.ofMillis(900000L);
    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        redisTokenService = new RedisTokenService(stringRedisTemplate, jwtUtils);
        ReflectionTestUtils.setField(redisTokenService, "passwordResetEmailCooldownMs", 120000L);
        ReflectionTestUtils.setField(redisTokenService, "passwordResetTokenExpirationMs", 900000L);
        ReflectionTestUtils.setField(redisTokenService, "passwordResetIpRateLimitWindowMs", 900000L);
        ReflectionTestUtils.setField(redisTokenService, "passwordResetIpRateLimitMaxRequests", 10L);
        ReflectionTestUtils.setField(redisTokenService, "jwtExpirationMs", 3600000L);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Blacklist access token stores hashed token until token expiration")
    void blacklistAccessToken_WithFutureExpiration_StoresToken() {
        Date expiration = Date.from(Instant.now().plusSeconds(60));
        when(jwtUtils.getExpirationFromJwtToken("access-token")).thenReturn(expiration);

        redisTokenService.blacklistAccessToken("access-token");

        verify(valueOperations).set(
                argThat(key -> key.startsWith("auth:blacklist:")),
                eq("1"),
                any(Duration.class));
    }

    @Test
    @DisplayName("Blacklist access token skips storing expired token")
    void blacklistAccessToken_WithExpiredToken_DoesNothing() {
        Date expiration = Date.from(Instant.now().minusSeconds(60));
        when(jwtUtils.getExpirationFromJwtToken("expired-token")).thenReturn(expiration);

        redisTokenService.blacklistAccessToken("expired-token");

        verify(valueOperations, never()).set(argThat(key -> key.startsWith("auth:blacklist:")), eq("1"), any(Duration.class));
    }

    @Test
    @DisplayName("Access token blacklist check returns true when key exists")
    void isAccessTokenBlacklisted_WhenKeyExists_ReturnsTrue() {
        when(stringRedisTemplate.hasKey(argThat(key -> key.startsWith("auth:blacklist:")))).thenReturn(true);

        boolean blacklisted = redisTokenService.isAccessTokenBlacklisted("access-token");

        assertTrue(blacklisted);
    }

    @Test
    @DisplayName("Invalidate user tokens stores invalid-after timestamp with JWT TTL")
    void invalidateUserTokens_StoresTimestamp() {
        redisTokenService.invalidateUserTokens(USER_ID);

        verify(valueOperations).set(
                eq("auth:user-token-invalid-after:" + USER_ID),
                argThat(value -> value != null && !value.isBlank()),
                eq(Duration.ofMillis(3600000L)));
    }

    @Test
    @DisplayName("User token invalidation returns false when no invalidation exists")
    void wasIssuedBeforeUserInvalidation_WhenMissingInvalidation_ReturnsFalse() {
        when(valueOperations.get("auth:user-token-invalid-after:" + USER_ID)).thenReturn(null);

        boolean invalidated = redisTokenService.wasIssuedBeforeUserInvalidation(USER_ID, Instant.now());

        assertFalse(invalidated);
    }

    @Test
    @DisplayName("User token invalidation returns true when issued before invalidation")
    void wasIssuedBeforeUserInvalidation_WhenIssuedBeforeInvalidation_ReturnsTrue() {
        Instant issuedAt = Instant.ofEpochMilli(1_000L);
        when(valueOperations.get("auth:user-token-invalid-after:" + USER_ID)).thenReturn("2000");

        boolean invalidated = redisTokenService.wasIssuedBeforeUserInvalidation(USER_ID, issuedAt);

        assertTrue(invalidated);
    }

    @Test
    @DisplayName("Create password reset token keeps current pointer untouched when no active token exists")
    void createPasswordResetToken_WithoutExistingActiveToken_DoesNotDeleteOldToken() {
        User user = new User();
        user.setId(USER_ID);
        when(valueOperations.get("auth:user-active-password-reset-token:" + USER_ID)).thenReturn(null);

        String token = redisTokenService.createPasswordResetToken(user);

        assertTrue(token != null && !token.isBlank());
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("Allow password reset requests while IP is within the rate limit window")
    void isPasswordResetIpRateLimitExceeded_WithinLimit_ReturnsFalse() {
        when(valueOperations.increment(argThat(key -> key.startsWith("auth:password-reset-ip-rate-limit:"))))
                .thenReturn(1L);

        boolean exceeded = redisTokenService.isPasswordResetIpRateLimitExceeded("127.0.0.1");

        assertFalse(exceeded);
        verify(stringRedisTemplate).expire(
                argThat(key -> key.startsWith("auth:password-reset-ip-rate-limit:")),
                eq(IP_RATE_LIMIT_WINDOW));
    }

    @Test
    @DisplayName("Reject password reset requests when IP exceeds the rate limit window")
    void isPasswordResetIpRateLimitExceeded_OverLimit_ReturnsTrue() {
        when(valueOperations.increment(argThat(key -> key.startsWith("auth:password-reset-ip-rate-limit:"))))
                .thenReturn(11L);

        boolean exceeded = redisTokenService.isPasswordResetIpRateLimitExceeded("127.0.0.1");

        assertTrue(exceeded);
        verify(stringRedisTemplate, never()).expire(
                argThat(key -> key.startsWith("auth:password-reset-ip-rate-limit:")),
                eq(IP_RATE_LIMIT_WINDOW));
    }

    @Test
    @DisplayName("Reject password reset requests when rate limit increment fails")
    void isPasswordResetIpRateLimitExceeded_WhenIncrementFails_ThrowsException() {
        when(valueOperations.increment(argThat(key -> key.startsWith("auth:password-reset-ip-rate-limit:"))))
                .thenReturn(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> redisTokenService.isPasswordResetIpRateLimitExceeded("127.0.0.1"));

        assertEquals("Failed to increment password reset IP rate limit", exception.getMessage());
    }

    @Test
    @DisplayName("Acquire password reset email cooldown when email is not cooling down")
    void tryAcquirePasswordResetEmailCooldown_Available_ReturnsTrue() {
        when(valueOperations.setIfAbsent(
                argThat(key -> key.startsWith("auth:password-reset-email-cooldown:")),
                eq("1"),
                eq(Duration.ofMillis(120000L))))
                .thenReturn(true);

        boolean acquired = redisTokenService.tryAcquirePasswordResetEmailCooldown(" User@Gmail.com ");

        assertTrue(acquired);
        verify(valueOperations).setIfAbsent(
                argThat(key -> key.startsWith("auth:password-reset-email-cooldown:")),
                eq("1"),
                eq(Duration.ofMillis(120000L)));
    }

    @Test
    @DisplayName("Reject password reset email cooldown acquisition when already cooling down")
    void tryAcquirePasswordResetEmailCooldown_AlreadyCoolingDown_ReturnsFalse() {
        when(valueOperations.setIfAbsent(
                argThat(key -> key.startsWith("auth:password-reset-email-cooldown:")),
                eq("1"),
                eq(Duration.ofMillis(120000L))))
                .thenReturn(false);

        boolean acquired = redisTokenService.tryAcquirePasswordResetEmailCooldown("user2@gmail.com");

        assertFalse(acquired);
    }

    @Test
    @DisplayName("Create password reset token invalidates the previously active token for the user")
    void createPasswordResetToken_ReplacesExistingActiveToken() {
        User user = new User();
        user.setId(USER_ID);

        when(valueOperations.get("auth:user-active-password-reset-token:" + USER_ID)).thenReturn("old-token");

        String token = redisTokenService.createPasswordResetToken(user);

        assertTrue(token != null && !token.isBlank());
        verify(stringRedisTemplate).delete("auth:password-reset:old-token");
        verify(valueOperations).set(eq("auth:password-reset:" + token), eq(USER_ID), eq(PASSWORD_RESET_TTL));
        verify(valueOperations).set(eq("auth:user-active-password-reset-token:" + USER_ID), eq(token), eq(PASSWORD_RESET_TTL));
    }

    @Test
    @DisplayName("Delete password reset token removes active token pointer when token is current")
    void deletePasswordResetToken_CurrentToken_DeletesUserPointer() {
        when(valueOperations.get("auth:password-reset:token-123")).thenReturn(USER_ID);
        when(valueOperations.get("auth:user-active-password-reset-token:" + USER_ID)).thenReturn("token-123");

        redisTokenService.deletePasswordResetToken("token-123");

        verify(stringRedisTemplate).delete("auth:password-reset:token-123");
        verify(stringRedisTemplate).delete("auth:user-active-password-reset-token:" + USER_ID);
    }

    @Test
    @DisplayName("Delete password reset token keeps active token pointer when token is not current")
    void deletePasswordResetToken_NonCurrentToken_KeepsUserPointer() {
        when(valueOperations.get("auth:password-reset:token-123")).thenReturn(USER_ID);
        when(valueOperations.get("auth:user-active-password-reset-token:" + USER_ID)).thenReturn("different-token");

        redisTokenService.deletePasswordResetToken("token-123");

        verify(stringRedisTemplate).delete("auth:password-reset:token-123");
        verify(stringRedisTemplate, never()).delete("auth:user-active-password-reset-token:" + USER_ID);
    }

    @Test
    @DisplayName("Delete password reset token stops when token has no user mapping")
    void deletePasswordResetToken_WithoutUserMapping_StopsEarly() {
        when(valueOperations.get("auth:password-reset:token-123")).thenReturn(null);

        redisTokenService.deletePasswordResetToken("token-123");

        verify(stringRedisTemplate).delete("auth:password-reset:token-123");
        verify(valueOperations, never()).get("auth:user-active-password-reset-token:" + USER_ID);
    }

    @Test
    @DisplayName("Get password reset user id delegates to Redis lookup")
    void getPasswordResetUserId_ReturnsStoredValue() {
        when(valueOperations.get("auth:password-reset:token-123")).thenReturn(USER_ID);

        String userId = redisTokenService.getPasswordResetUserId("token-123");

        assertEquals(USER_ID, userId);
    }
}
