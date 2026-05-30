package com.ecommerce.sshop.mapper;

import org.mapstruct.Mapper;

import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.model.user.User;

@Mapper(componentModel = "spring", uses = { OrderMapper.class, CartMapper.class })
public interface UserMapper {
    UserDto toDto(User user);
}
