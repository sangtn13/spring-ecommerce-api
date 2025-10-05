package com.ecommerce.sshop.service.user;

import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.request.users.CreateUserRequest;
import com.ecommerce.sshop.request.users.CreateUserWithRoleRequest;
import com.ecommerce.sshop.request.users.UpdateUserRequest;

public interface IUserService {

    User getUserById(Long userId);

    User createUser(CreateUserRequest request);

    User createUserWithRole(CreateUserWithRoleRequest request);

    User updateUser(UpdateUserRequest request, Long userId);

    void deleteUser(Long userId);

    UserDto convertUserToDto(User user);

    User getCurrentUser();
}
