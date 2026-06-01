package com.ecommerce.sshop.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.mapper.UserMapper;
import com.ecommerce.sshop.model.role.Role;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.role.IRoleRepository;
import com.ecommerce.sshop.repository.user.IUserRepository;
import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.request.users.CreateUserWithRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserRequest;
import com.ecommerce.sshop.exception.user.UserNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private IUserRepository userRepository;
    @Mock private IRoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserService userService;

    private User sampleUser;
    private Role userRole;
    private final String userId = "user-uuid-123";
    private final String email = "sangtn@gmail.com";

    @BeforeEach
    void setUp() {
        userRole = new Role("User");
        sampleUser = new User();
        sampleUser.setId(userId);
        sampleUser.setEmail(email);
        sampleUser.setFirstName("Sang");
        sampleUser.setLastName("Tran");
        sampleUser.setPassword("encoded_pass");
        sampleUser.setRoles(Set.of(userRole));
    }

    @Test
    @DisplayName("Create new user successfully")
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setPassword("password123");
        request.setFirstName("Sang");
        request.setLastName("Tran");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName("User")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Create user fails when email already exists")
    void createUser_EmailAlreadyExists_ThrowsException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create user with specific role successfully")
    void createUserWithRole_Admin_Success() {
        CreateUserWithRoleRequest request = new CreateUserWithRoleRequest();
        request.setEmail("admin@gmail.com");
        request.setRole("Admin");
        request.setPassword("admin123");

        Role adminRole = new Role("Admin");
        User adminUser = new User();
        adminUser.setEmail("admin@gmail.com");
        adminUser.setRoles(Set.of(adminRole));

        when(userRepository.existsByEmail("admin@gmail.com")).thenReturn(false);
        when(roleRepository.findByName("Admin")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        User result = userService.createUserWithRole(request);
        assertNotNull(result);
        assertTrue(result.getRoles().stream().anyMatch(r -> r.getName().equals("Admin")));
    }

    @Test
    @DisplayName("Update user information successfully")
    void updateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Sang New");

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(request, userId);

        assertEquals("Sang New", result.getFirstName());
    }

    @Test
    @DisplayName("Get current user information successfully")
    void getCurrentUser_Success() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        when(authentication.getName()).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findByEmail(email)).thenReturn(sampleUser);

        User result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserById_SuccessAndNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        assertEquals(sampleUser, userService.getUserById(userId));

        when(userRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById("missing"));
    }

    @Test
    void createUser_RoleNotFound_ThrowsRuntimeException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setPassword("x");
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName("User")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.createUser(request));
    }

    @Test
    void createUserWithRole_DefaultsToUser_WhenInvalidRole() {
        CreateUserWithRoleRequest request = new CreateUserWithRoleRequest();
        request.setEmail("u2@gmail.com");
        request.setPassword("pw");
        request.setRole("UnknownRole");

        when(userRepository.existsByEmail("u2@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(roleRepository.findByName("User")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUserWithRole(request);
        assertTrue(result.getRoles().stream().anyMatch(r -> r.getName().equals("User")));
    }

    @Test
    void createUserWithRole_EmailExists_Throws() {
        CreateUserWithRoleRequest request = new CreateUserWithRoleRequest();
        request.setEmail(email);
        when(userRepository.existsByEmail(email)).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> userService.createUserWithRole(request));
    }

    @Test
    void deleteUser_SuccessAndNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        assertDoesNotThrow(() -> userService.deleteUser(userId));
        verify(userRepository).delete(sampleUser);

        when(userRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("missing"));
    }

    @Test
    void convertUserToDto_AndGetCurrentUserNotFound() {
        UserDto dto = new UserDto();
        when(userMapper.toDto(sampleUser)).thenReturn(dto);
        assertEquals(dto, userService.convertUserToDto(sampleUser));

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn("missing@gmail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser());
    }

    @Test
    void roleLockAndLoginTimeFlows() {
        Role managerRole = new Role("Manager");
        UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest();
        roleRequest.setRole("manager");

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(roleRepository.findByName("Manager")).thenReturn(Optional.of(managerRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User changedRole = userService.updateUserRole(roleRequest, userId);
        assertTrue(changedRole.getRoles().stream().anyMatch(r -> r.getName().equals("Manager")));

        User locked = userService.lockUser(userId);
        assertTrue(locked.getAccountLocked());

        User unlocked = userService.unlockUser(userId);
        assertFalse(unlocked.getAccountLocked());

        User updatedLogin = userService.updateLastLogin(userId);
        assertNotNull(updatedLogin.getLastLoginAt());
    }
}
