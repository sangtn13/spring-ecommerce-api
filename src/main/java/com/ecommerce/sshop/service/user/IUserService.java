package com.ecommerce.sshop.service.user;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.request.users.CreateUserWithRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserRequest;

public interface IUserService {

    User getUserById(String userId);

    User createUser(CreateUserRequest request);

    User createUserWithRole(CreateUserWithRoleRequest request);

    User updateUser(UpdateUserRequest request, String userId);

    User updateUserRole(UpdateUserRoleRequest request, String userId);

    void deleteUser(String userId);

    UserDto convertUserToDto(User user);

    User getCurrentUser();

    User lockUser(String userId);

    User unlockUser(String userId);

    User updateLastLogin(String userId);
}
