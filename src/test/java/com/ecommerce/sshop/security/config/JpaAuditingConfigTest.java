package com.ecommerce.sshop.security.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ecommerce.sshop.security.user.ShopUserDetails;

class JpaAuditingConfigTest {
    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditorProvider_CoversPrincipalBranches() {
        JpaAuditingConfig config = new JpaAuditingConfig();
        AuditorAware<String> aware = config.auditorProvider();

        assertEquals("system", aware.getCurrentAuditor().orElseThrow());

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("john", "n/a", List.of()));
        assertEquals("john", aware.getCurrentAuditor().orElseThrow());

        ShopUserDetails details = new ShopUserDetails("u1", "mail@test.com", "pw", true, List.of());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(details, "n/a", List.of()));
        assertEquals("mail@test.com", aware.getCurrentAuditor().orElseThrow());
    }
}
