package com.ecommerce.sshop.service.user;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.ecommerce.sshop.exception.AlreadyExistsException;
import com.ecommerce.sshop.exception.UserNotFoundException;
import com.ecommerce.sshop.model.User;
import com.ecommerce.sshop.request.CreateUserRequest;
import com.ecommerce.sshop.request.UpdateUserRequest;
import com.ecommerce.sshop.repository.IUserRepository;
import com.ecommerce.sshop.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public User getUserById(Long userId) {
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
                    newUser.setPassword(req.getPassword()); // In real applications, ensure to hash passwords
                    return userRepository.save(newUser);
                })
                .orElseThrow(() -> new AlreadyExistsException("User already exists with email: " + request.getEmail()));

    }

    @Override
    public User updateUser(UpdateUserRequest request, Long userId) {
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
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .ifPresentOrElse(userRepository::delete, () -> {
                    throw new UserNotFoundException("User not found with id: " + userId);
                });
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }
}
