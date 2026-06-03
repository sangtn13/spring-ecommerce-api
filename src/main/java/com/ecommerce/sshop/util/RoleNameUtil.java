package com.ecommerce.sshop.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.ecommerce.sshop.exception.user.InvalidUserRequestException;

public final class RoleNameUtil {
    private static final Set<String> ALLOWED_ROLES = Set.of("User", "Admin", "Manager");
    private static final String INVALID_ROLE_MESSAGE = "roles must contain only: User, Admin, Manager";

    private RoleNameUtil() {
    }

    public static String normalizeAllowedRole(String rawRole) {
        String normalizedRole = StringUtil.trimToNull(rawRole);
        if (normalizedRole == null) {
            return "User";
        }

        return canonicalizeAllowedRole(normalizedRole).orElse("User");
    }

    public static Set<String> normalizeRequiredRoles(Collection<String> rawRoles) {
        if (rawRoles == null || rawRoles.isEmpty()) {
            throw new InvalidUserRequestException("roles are required");
        }

        Set<String> normalizedRoles = rawRoles.stream()
                .map(StringUtil::trimToNull)
                .filter(role -> role != null)
                .map(RoleNameUtil::normalizeSingleRequiredRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalizedRoles.isEmpty()) {
            throw new InvalidUserRequestException("roles are required");
        }

        return normalizedRoles;
    }

    public static String normalizeSingleRequiredRole(String rawRole) {
        return canonicalizeAllowedRole(rawRole)
                .orElseThrow(() -> new InvalidUserRequestException(INVALID_ROLE_MESSAGE));
    }

    private static java.util.Optional<String> canonicalizeAllowedRole(String rawRole) {
        return ALLOWED_ROLES.stream()
                .filter(role -> role.equalsIgnoreCase(rawRole))
                .findFirst();
    }
}
