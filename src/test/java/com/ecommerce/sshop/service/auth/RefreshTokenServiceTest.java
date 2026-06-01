package com.ecommerce.sshop.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.ecommerce.sshop.model.auth.RefreshToken;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.auth.IRefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 60000L);
    }

    @Test
    void create_UsesSingleRowPerUser() {
        User user = new User();
        user.setId("u1");
        RefreshToken existing = new RefreshToken();
        existing.setId("rt1");
        when(refreshTokenRepository.findByUserId("u1")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken token = refreshTokenService.create(user);
        assertEquals("rt1", token.getId());
        assertNotNull(token.getToken());
        assertNull(token.getRevokedAt());
    }

    @Test
    void verify_ReturnsNullForInvalidRevokedExpired() {
        assertNull(refreshTokenService.verify("missing"));
        verify(refreshTokenRepository).findByToken("missing");

        RefreshToken revoked = new RefreshToken();
        revoked.setRevokedAt(LocalDateTime.now());
        when(refreshTokenRepository.findByToken("revoked")).thenReturn(Optional.of(revoked));
        assertNull(refreshTokenService.verify("revoked"));

        RefreshToken expired = new RefreshToken();
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        expired.setRevokedAt(null);
        expired.setUser(new User());
        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));
        assertNull(refreshTokenService.verify("expired"));
    }

    @Test
    void verify_ThrowsWhenUserLocked_AndReturnsUserWhenValid() {
        User locked = new User();
        locked.setAccountLocked(true);
        RefreshToken token1 = new RefreshToken();
        token1.setUser(locked);
        token1.setExpiresAt(LocalDateTime.now().plusMinutes(1));
        when(refreshTokenRepository.findByToken("locked")).thenReturn(Optional.of(token1));
        assertThrows(LockedException.class, () -> refreshTokenService.verify("locked"));

        User ok = new User();
        ok.setAccountLocked(false);
        RefreshToken token2 = new RefreshToken();
        token2.setUser(ok);
        token2.setExpiresAt(LocalDateTime.now().plusMinutes(1));
        when(refreshTokenRepository.findByToken("ok")).thenReturn(Optional.of(token2));
        assertEquals(ok, refreshTokenService.verify("ok"));
    }

    @Test
    void revoke_SetsRevokedAtOnlyOnce() {
        RefreshToken token = new RefreshToken();
        when(refreshTokenRepository.findByToken("t")).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        refreshTokenService.revoke("t");
        assertNotNull(token.getRevokedAt());
        LocalDateTime first = token.getRevokedAt();
        refreshTokenService.revoke("t");
        assertEquals(first, token.getRevokedAt());
    }
}
