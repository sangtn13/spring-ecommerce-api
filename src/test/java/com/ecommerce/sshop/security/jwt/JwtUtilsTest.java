package com.ecommerce.sshop.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import com.ecommerce.sshop.security.user.ShopUserDetails;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private final String secretKey = "g0qlJwfjNUHoDn4YOos9jItP5/srQ3QXbPwJjzQFfyTTKpVH+NRLFSGgErlYp3KnThZ+tXBmHms5ysdmk8WL6g==";
    private final int expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        // Inject value for private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secretKey);
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", expirationMs);
    }

    @Test
    @DisplayName("Generate and validate JWT Token successfully")
    void generateAndValidateToken_Success() {
        Authentication authentication = mock(Authentication.class);
        ShopUserDetails userDetails = new ShopUserDetails(
                "user-123", "sangtn@gmail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // 1. Test create JWT Token
        String token = jwtUtils.generateTokenForUser(authentication);
        assertNotNull(token);

        // 2. Test decode to get Username from Token
        String username = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals("sangtn@gmail.com", username);

        // 3. Test validate JWT Token
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    @DisplayName("Validate JWT Token fails when passing an invalid token")
    void validateToken_Invalid_ThrowsException() {
        String invalidToken = "completely-wrong-token-structure";

        assertThrows(JwtException.class, () -> {
            jwtUtils.validateJwtToken(invalidToken);
        });
    }
}