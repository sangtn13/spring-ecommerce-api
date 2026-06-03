package com.ecommerce.sshop.controller.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.exception.user.InvalidUserRequestException;
import com.ecommerce.sshop.exception.user.UserNotFoundException;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.request.users.CreateUserWithRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserLockRequest;
import com.ecommerce.sshop.request.users.UpdateUserRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.user.IUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private IUserService userService;

    @InjectMocks private UserController userController;

    private User mockUser;
    private UserDto mockUserDto;
    private final String userId = "user-123";

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(userId);
        mockUserDto = new UserDto();
    }

    @Test
    @DisplayName("Get current user profile successfully (Auth-Profile)")
    void getCurrentUserProfile_Success() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        ResponseEntity<ApiResponse> response = userController.getCurrentUserProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User profile retrieved successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Get users with paging successfully (Admin)")
    void getUsers_Success() {
        Page<UserDto> page = new PageImpl<>(java.util.List.of(mockUserDto));
        when(userService.getAllUsersWithPaging(any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse> response = userController.getUsers(1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Users retrieved successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Get user by ID successfully (Admin)")
    void getUserById_Success() {
        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        ResponseEntity<ApiResponse> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Get user by ID failed when no result is returned (Admin)")
    void getUserById_NotFound() {
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found with id: " + userId));
        assertThrows(UserNotFoundException.class, () -> userController.getUserById(userId));
    }

    @Test
    @DisplayName("Create new user with specified role successfully (Admin)")
    void createUser_Success() {
        CreateUserWithRoleRequest request = new CreateUserWithRoleRequest();
        when(userService.createUserWithRole(request)).thenReturn(mockUser);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        ResponseEntity<ApiResponse> response = userController.createUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User created successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Update user information successfully (Admin)")
    void updateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest();
        when(userService.updateUser(request, userId)).thenReturn(mockUser);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        ResponseEntity<ApiResponse> response = userController.updateUser(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Update user role successfully (Admin)")
    void updateUserRole_Success() {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRoles(java.util.Set.of("Manager", "User"));
        when(userService.updateUserRole(request, userId)).thenReturn(mockUser);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        ResponseEntity<ApiResponse> response = userController.updateUserRole(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User roles updated successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Update user role fails when request body is missing")
    void updateUserRole_NullRequest_ThrowsInvalidUserRequestException() {
        when(userService.updateUserRole(null, userId))
                .thenThrow(new InvalidUserRequestException("roles request body is required"));
        InvalidUserRequestException exception = assertThrows(InvalidUserRequestException.class,
                () -> userController.updateUserRole(userId, null));

        assertEquals("roles request body is required", exception.getMessage());
        verify(userService).updateUserRole(null, userId);
    }

    @Test
    @DisplayName("Delete user successfully (Admin)")
    void deleteUser_Success() {
        doNothing().when(userService).deleteUser(userId);

        ResponseEntity<ApiResponse> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Lock user successfully (Admin)")
    void updateUserLock_Locked_Success() {
        UpdateUserLockRequest request = new UpdateUserLockRequest();
        request.setLocked(true);
        mockUser.setAccountLocked(true);
        when(userService.updateUserLock(request, userId)).thenReturn(mockUser);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        ResponseEntity<ApiResponse> response = userController.updateUserLock(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User locked successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Unlock user successfully (Admin)")
    void updateUserLock_Unlocked_Success() {
        UpdateUserLockRequest request = new UpdateUserLockRequest();
        request.setLocked(false);
        mockUser.setAccountLocked(false);
        when(userService.updateUserLock(request, userId)).thenReturn(mockUser);
        when(userService.convertUserToDto(mockUser)).thenReturn(mockUserDto);

        ResponseEntity<ApiResponse> response = userController.updateUserLock(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User unlocked successfully", response.getBody().getMessage());
    }
}
