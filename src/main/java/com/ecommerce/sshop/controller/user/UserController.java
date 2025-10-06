package com.ecommerce.sshop.controller.user;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.request.users.CreateUserWithRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.user.IUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/my-profile")
    public ResponseEntity<ApiResponse> getCurrentUserProfile() {
        User user = userService.getCurrentUser();
        UserDto userDto = userService.convertUserToDto(user);
        return ResponseEntity.ok(new ApiResponse("User profile retrieved successfully", userDto));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        UserDto userDto = userService.convertUserToDto(user);
        if (user != null) {
            return ResponseEntity.ok(new ApiResponse("User retrieved successfully", userDto));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse("User not found", null));
        }
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping()
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserWithRoleRequest user) {
        User createdUser = userService.createUserWithRole(user);
        UserDto userDto = userService.convertUserToDto(createdUser);
        return ResponseEntity.ok(new ApiResponse("User created successfully", userDto));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest user) {
        User updatedUser = userService.updateUser(user, userId);
        if (updatedUser != null) {
            UserDto userDto = userService.convertUserToDto(updatedUser);
            return ResponseEntity.ok(new ApiResponse("User updated successfully", userDto));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse("User not found", null));
        }
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse("User deleted successfully", null));
    }
}
