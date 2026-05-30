package com.ecommerce.sshop.security.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashSet;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.user.IUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class ShopUserDetailsServiceTest {

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private ShopUserDetailsService shopUserDetailsService;

    @Test
    @DisplayName("If user is found, loadUserByUsername should return UserDetails")
    void loadUserByUsername_Success() {
        String email = "sangtn@gmail.com";
        User mockUser = new User();
        mockUser.setId("user-id-123");
        mockUser.setEmail(email);
        mockUser.setPassword("encoded-password");
        mockUser.setRoles(new HashSet<>());

        when(userRepository.findByEmail(email)).thenReturn(mockUser);

        UserDetails result = shopUserDetailsService.loadUserByUsername(email);

        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertEquals("encoded-password", result.getPassword());
    }

    @Test
    @DisplayName("If user not found, loadUserByUsername should throw UsernameNotFoundException")
    void loadUserByUsername_NotFound_ThrowsException() {
        String email = "unknown@gmail.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            shopUserDetailsService.loadUserByUsername(email);
        });
    }
}