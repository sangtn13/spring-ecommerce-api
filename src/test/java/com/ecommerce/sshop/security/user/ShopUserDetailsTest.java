package com.ecommerce.sshop.security.user;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Set;
import com.ecommerce.sshop.model.role.Role;
import com.ecommerce.sshop.model.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShopUserDetailsTest {

    @Test
    @DisplayName("Check all getter methods and account status of ShopUserDetails")
    void testShopUserDetailsFieldsAndMethods() {
        User user = new User();
        user.setId("user-123");
        user.setEmail("sangtn@gmail.com");
        user.setPassword("hashed_password");
        user.setRoles(Set.of(new Role("Admin")));

        ShopUserDetails userDetails = ShopUserDetails.buildUserDetails(user);

        assertNotNull(userDetails);
        assertEquals("user-123", userDetails.getId());
        assertEquals("sangtn@gmail.com", userDetails.getUsername());
        assertEquals("hashed_password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        
        // Assert that the account is active and not expired/locked
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
        
        // Check equals and hashCode based on id
        ShopUserDetails emptyDetails = new ShopUserDetails();
        assertNull(emptyDetails.getId());
    }
}