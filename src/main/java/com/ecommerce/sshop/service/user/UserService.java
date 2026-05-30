package com.ecommerce.sshop.service.user;

import java.util.Optional;
import java.util.Set;

import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.exception.user.UserNotFoundException;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.request.users.CreateUserWithRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserRequest;
import com.ecommerce.sshop.repository.user.IUserRepository;
import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.repository.role.IRoleRepository;
import com.ecommerce.sshop.model.role.Role;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final IRoleRepository roleRepository;

    @Override
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    @Override
    public User createUser(CreateUserRequest request) {
        return Optional.of(request)
                .filter(req -> !userRepository.existsByEmail(req.getEmail()))
                .map(req -> {
                    User newUser = new User();
                    newUser.setFirstName(req.getFirstName());
                    newUser.setLastName(req.getLastName());
                    newUser.setEmail(req.getEmail());
                    newUser.setPassword(passwordEncoder.encode(req.getPassword()));

                    // Set default role as "User"
                    Role userRole = roleRepository.findByName("User")
                            .orElseThrow(() -> new RuntimeException("Role 'User' not found"));
                    newUser.setRoles(Set.of(userRole));

                    return userRepository.save(newUser);
                })
                .orElseThrow(() -> new AlreadyExistsException("User already exists with email: " + request.getEmail()));

    }

    @Override
    public User createUserWithRole(CreateUserWithRoleRequest request) {
        return Optional.of(request)
                .filter(req -> !userRepository.existsByEmail(req.getEmail()))
                .map(req -> {
                    User newUser = new User();
                    newUser.setFirstName(req.getFirstName());
                    newUser.setLastName(req.getLastName());
                    newUser.setEmail(req.getEmail());
                    newUser.setPassword(passwordEncoder.encode(req.getPassword()));

                    // Set role based on request, default to "User" if not specified
                    String requestedRole = (req.getRole() != null && !req.getRole().isEmpty()) ? req.getRole() : "User";
                    final String roleName = (requestedRole.equals("User") || requestedRole.equals("Admin"))
                            ? requestedRole
                            : "User";

                    Role role = roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role '" + roleName + "' not found"));
                    newUser.setRoles(Set.of(role));

                    return userRepository.save(newUser);
                })
                .orElseThrow(() -> new AlreadyExistsException("User already exists with email: " + request.getEmail()));
    }

    @Override
    public User updateUser(UpdateUserRequest request, String userId) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (request.getFirstName() != null) {
            existingUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            existingUser.setLastName(request.getLastName());
        }

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(String userId) {
        userRepository.findById(userId)
                .ifPresentOrElse(userRepository::delete, () -> {
                    throw new UserNotFoundException("User not found with id: " + userId);
                });
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        return user;
    }
}
