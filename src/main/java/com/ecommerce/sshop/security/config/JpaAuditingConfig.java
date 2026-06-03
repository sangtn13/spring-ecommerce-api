package com.ecommerce.sshop.security.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ecommerce.sshop.security.user.ShopUserDetails;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
    private static final String SYSTEM_AUDITOR = "system";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of(SYSTEM_AUDITOR);
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof ShopUserDetails shopUserDetails) {
                return Optional.ofNullable(shopUserDetails.getEmail()).or(() -> Optional.of(SYSTEM_AUDITOR));
            }
            if (principal instanceof String principalText && !"anonymousUser".equals(principalText)) {
                return Optional.of(principalText);
            }

            return Optional.of(SYSTEM_AUDITOR);
        };
    }
}
