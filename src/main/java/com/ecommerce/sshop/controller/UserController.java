package com.ecommerce.sshop.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.http.ResponseEntity;
import com.ecommerce.sshop.exception.AlreadyExistsException;
import com.ecommerce.sshop.exception.UserNotFoundException;
import com.ecommerce.sshop.model.User;
import com.ecommerce.sshop.dto.UserDto;
import com.ecommerce.sshop.request.CreateUserRequest;
import com.ecommerce.sshop.request.UpdateUserRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.user.IUserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            UserDto userDto = userService.convertUserToDto(user);
            if (user != null) {
                return ResponseEntity.ok(new ApiResponse("User retrieved successfully", userDto));
            } else {
                return ResponseEntity.status(404).body(new ApiResponse("User not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("Error retrieving user", null));
        }
    }

    @PostMapping()
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserRequest user) {
        try {
            User createdUser = userService.createUser(user);
            UserDto userDto = userService.convertUserToDto(createdUser);
            return ResponseEntity.ok(new ApiResponse("User created successfully", userDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(409).body(new ApiResponse("User already exists", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("Error creating user", null));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest user) {
        try {
            User updatedUser = userService.updateUser(user, userId);
            if (updatedUser != null) {
                UserDto userDto = userService.convertUserToDto(updatedUser);
                return ResponseEntity.ok(new ApiResponse("User updated successfully", userDto));
            } else {
                return ResponseEntity.status(404).body(new ApiResponse("User not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("Error updating user", null));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ApiResponse("User deleted successfully", null));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(new ApiResponse("User not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("Error deleting user", null));
        }
    }
}
