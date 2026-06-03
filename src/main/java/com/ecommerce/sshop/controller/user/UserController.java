package com.ecommerce.sshop.controller.user;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.request.users.CreateUserWithRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserLockRequest;
import com.ecommerce.sshop.request.users.UpdateUserRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.PagedResponse;
import com.ecommerce.sshop.service.user.IUserService;
import com.ecommerce.sshop.util.PageUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUserProfile() {
        User user = userService.getCurrentUser();
        UserDto userDto = userService.convertUserToDto(user);
        return ResponseEntity.ok(new ApiResponse("User profile retrieved successfully", userDto));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping()
    public ResponseEntity<ApiResponse> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<UserDto> userPage = userService.getAllUsersWithPaging(pageable);
        return ResponseEntity.ok(new ApiResponse("Users retrieved successfully", PagedResponse.of(userPage)));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        UserDto userDto = userService.convertUserToDto(user);
        return ResponseEntity.ok(new ApiResponse("User retrieved successfully", userDto));
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
    public ResponseEntity<ApiResponse> updateUser(@PathVariable String userId, @RequestBody UpdateUserRequest user) {
        User updatedUser = userService.updateUser(user, userId);
        UserDto userDto = userService.convertUserToDto(updatedUser);
        return ResponseEntity.ok(new ApiResponse("User updated successfully", userDto));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse> updateUserRole(@PathVariable String userId, @RequestBody UpdateUserRoleRequest request) {
        User updatedUser = userService.updateUserRole(request, userId);
        UserDto userDto = userService.convertUserToDto(updatedUser);
        return ResponseEntity.ok(new ApiResponse("User roles updated successfully", userDto));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse("User deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/{userId}/account-lock")
    public ResponseEntity<ApiResponse> updateUserLock(@PathVariable String userId,
            @RequestBody UpdateUserLockRequest request) {
        User user = userService.updateUserLock(request, userId);
        UserDto userDto = userService.convertUserToDto(user);
        String message = Boolean.TRUE.equals(user.getAccountLocked())
                ? "User locked successfully"
                : "User unlocked successfully";
        return ResponseEntity.ok(new ApiResponse(message, userDto));
    }
}
