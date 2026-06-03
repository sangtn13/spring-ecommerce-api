package com.ecommerce.sshop.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import com.ecommerce.sshop.model.role.Role;
import com.ecommerce.sshop.security.user.ShopUserDetails;

import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private final String secretKey = "g0qlJwfjNUHoDn4YOos9jItP5/srQ3QXbPwJjzQFfyTTKpVH+NRLFSGgErlYp3KnThZ+tXBmHms5ysdmk8WL6g==";
    private final int expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secretKey);
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", expirationMs);
    }

    @Test
    @DisplayName("Generate and validate JWT token successfully")
    void generateAndValidateToken_Success() {
        Authentication authentication = mock(Authentication.class);
        ShopUserDetails userDetails = new ShopUserDetails(
                "user-123",
                "sangtn@gmail.com",
                "password",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtUtils.generateTokenForUser(authentication);

        assertNotNull(token);
        assertEquals("sangtn@gmail.com", jwtUtils.getUserNameFromJwtToken(token));
        assertEquals("user-123", jwtUtils.getUserIdFromJwtToken(token));
        assertNotNull(jwtUtils.getIssuedAtFromJwtToken(token));
        assertNotNull(jwtUtils.getExpirationFromJwtToken(token));
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    @DisplayName("Validate JWT token fails when passing an invalid token")
    void validateToken_Invalid_ThrowsException() {
        String invalidToken = "completely-wrong-token-structure";

        assertThrows(JwtException.class, () -> jwtUtils.validateJwtToken(invalidToken));
    }

    @Test
    void generateTokenForUserEmail_Success() {
        Role role = new Role("Admin");
        String token = jwtUtils.generateTokenForUserEmail("admin@test.com", "u-1", Set.of(role));
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("admin@test.com", jwtUtils.getUserNameFromJwtToken(token));
        assertEquals("u-1", jwtUtils.getUserIdFromJwtToken(token));
    }
}
