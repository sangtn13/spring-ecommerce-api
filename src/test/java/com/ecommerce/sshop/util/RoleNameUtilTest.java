package com.ecommerce.sshop.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import com.ecommerce.sshop.exception.user.InvalidUserRequestException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleNameUtilTest {

    @Test
    @DisplayName("normalizeAllowedRole defaults invalid or blank role to User")
    void normalizeAllowedRole_DefaultsToUser() {
        assertEquals("User", RoleNameUtil.normalizeAllowedRole(null));
        assertEquals("User", RoleNameUtil.normalizeAllowedRole("  "));
        assertEquals("User", RoleNameUtil.normalizeAllowedRole("SuperAdmin"));
    }

    @Test
    @DisplayName("normalizeAllowedRole canonicalizes valid role casing")
    void normalizeAllowedRole_CanonicalizesCase() {
        assertEquals("Admin", RoleNameUtil.normalizeAllowedRole("admin"));
        assertEquals("Manager", RoleNameUtil.normalizeAllowedRole("MANAGER"));
    }

    @Test
    @DisplayName("normalizeRequiredRoles trims, deduplicates, and canonicalizes roles")
    void normalizeRequiredRoles_NormalizesValues() {
        assertEquals(Set.of("Admin", "User"), RoleNameUtil.normalizeRequiredRoles(List.of(" admin ", "User", "ADMIN")));
    }

    @Test
    @DisplayName("normalizeRequiredRoles rejects invalid role values")
    void normalizeRequiredRoles_InvalidRole_ThrowsException() {
        InvalidUserRequestException exception = assertThrows(InvalidUserRequestException.class,
                () -> RoleNameUtil.normalizeRequiredRoles(List.of("SuperAdmin")));
        assertEquals("roles must contain only: User, Admin, Manager", exception.getMessage());
    }
}
