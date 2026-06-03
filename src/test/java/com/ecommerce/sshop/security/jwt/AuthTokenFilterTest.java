package com.ecommerce.sshop.security.jwt;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecommerce.sshop.security.user.ShopUserDetailsService;
import com.ecommerce.sshop.service.auth.RedisTokenService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private ShopUserDetailsService userDetailsService;
    @Mock
    private RedisTokenService redisTokenService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private AuthTokenFilter authTokenFilter;

    @BeforeEach
    void setUp() {
        authTokenFilter = new AuthTokenFilter(jwtUtils, userDetailsService, redisTokenService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Filter passes through when no Authorization header is present")
    void doFilterInternal_NoToken_PassesThrough() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Filter authenticates and loads user information into security context successfully")
    void doFilterInternal_ValidToken_Authenticates() throws Exception {
        String token = "valid-token-string";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(redisTokenService.isAccessTokenBlacklisted(token)).thenReturn(false);
        when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn("user-123");
        when(jwtUtils.getIssuedAtFromJwtToken(token)).thenReturn(new Date());
        when(redisTokenService.wasIssuedBeforeUserInvalidation(any(), any())).thenReturn(false);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("sangtn@gmail.com");

        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getAuthorities()).thenReturn(Collections.emptyList());
        when(userDetailsService.loadUserByUsername("sangtn@gmail.com")).thenReturn(mockUserDetails);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter blocks and returns HTTP 401 when token is blacklisted")
    void doFilterInternal_BlacklistedToken_Returns401() throws Exception {
        String token = "blacklisted-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(redisTokenService.isAccessTokenBlacklisted(token)).thenReturn(true);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter blocks and returns HTTP 401 when token was issued before user invalidation")
    void doFilterInternal_UserInvalidatedToken_Returns401() throws Exception {
        String token = "invalidated-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(redisTokenService.isAccessTokenBlacklisted(token)).thenReturn(false);
        when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn("user-123");
        when(jwtUtils.getIssuedAtFromJwtToken(token)).thenReturn(new Date());
        when(redisTokenService.wasIssuedBeforeUserInvalidation(eq("user-123"), any())).thenReturn(true);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter blocks and returns HTTP 401 when token is invalid or expired")
    void doFilterInternal_JwtException_Returns401() throws Exception {
        String token = "expired-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenThrow(new JwtException("Token expired"));

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }
}
