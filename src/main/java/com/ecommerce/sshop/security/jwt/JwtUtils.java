package com.ecommerce.sshop.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Collection;

import com.ecommerce.sshop.model.role.Role;
import com.ecommerce.sshop.security.user.ShopUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    @Value("${sshop.app.jwtSecret}")
    private String jwtSecret;

    @Value("${sshop.app.jwtExpirationMs}")
    private int expirationTime;

    public String generateTokenForUser(Authentication authentication) {
        ShopUserDetails userPrincipal = (ShopUserDetails) authentication.getPrincipal();

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(userPrincipal.getEmail())
                .claim("id", userPrincipal.getId())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationTime))
                .signWith(key(), SignatureAlgorithm.HS512).compact();
    }

    public String generateTokenForUserEmail(String email, String userId, Collection<Role> userRoles) {
        List<String> roles = userRoles.stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .setSubject(email)
                .claim("id", userId)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationTime))
                .signWith(key(), SignatureAlgorithm.HS512).compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUserIdFromJwtToken(String token) {
        return parseClaims(token).get("id", String.class);
    }

    public Date getIssuedAtFromJwtToken(String token) {
        return parseClaims(token).getIssuedAt();
    }

    public Date getExpirationFromJwtToken(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            parseClaims(authToken);
            return true;
        } catch (Exception e) {
            throw new JwtException("Invalid JWT token: " + e.getMessage());
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
    }
}
