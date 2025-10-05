package com.ecommerce.sshop.security.user;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.user.IUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopUserDetailsService implements UserDetailsService {
    private final IUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return ShopUserDetails.buildUserDetails(user);
    }
}
