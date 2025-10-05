package com.ecommerce.sshop.data;

import java.util.Set;

import com.ecommerce.sshop.repository.role.IRoleRepository;
import com.ecommerce.sshop.repository.user.IUserRepository;
import com.ecommerce.sshop.model.role.Role;
import com.ecommerce.sshop.model.user.User;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Transactional
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        createDefaultRolesIfNotExists(Set.of("User", "Admin"));
        createDefaultUserIfNotExists();
        createDefaultAdminIfNotExists();
    }

    private void createDefaultUserIfNotExists() {
        Role userRole = roleRepository.findByName("User").get();
        for (int i = 1; i <= 5; i++) {
            String defaultEmail = "user" + i + "@gmail.com";
            if (userRepository.existsByEmail(defaultEmail)) {
                continue;
            }
            User user = new User();
            user.setFirstName("Sshop");
            user.setLastName("User" + i);
            user.setEmail(defaultEmail);
            user.setPassword(passwordEncoder.encode("123456" + i));
            user.setRoles(Set.of(userRole));
            userRepository.save(user);
        }
    }

    private void createDefaultAdminIfNotExists() {
        Role adminRole = roleRepository.findByName("Admin").get();
        String adminEmail = "admin@gmail.com";
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User user = new User();
        user.setFirstName("Sshop");
        user.setLastName("Admin");
        user.setEmail(adminEmail);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRoles(Set.of(adminRole));
        userRepository.save(user);
    }

    private void createDefaultRolesIfNotExists(Set<String> roleNames) {
        roleNames.stream()
                .filter(roleName -> roleRepository.findByName(roleName).isEmpty())
                .map(Role::new).forEach(roleRepository::save);

    }

}
