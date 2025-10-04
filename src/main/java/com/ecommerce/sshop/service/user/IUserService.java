package com.ecommerce.sshop.service.user;

import com.ecommerce.sshop.model.User;
import com.ecommerce.sshop.dto.UserDto;
import com.ecommerce.sshop.request.CreateUserRequest;
import com.ecommerce.sshop.request.UpdateUserRequest;

public interface IUserService {

    User getUserById(Long userId);
    User createUser(CreateUserRequest request);
    User updateUser(UpdateUserRequest request, Long userId);
    void deleteUser(Long userId);
    UserDto convertUserToDto(User user);
}
