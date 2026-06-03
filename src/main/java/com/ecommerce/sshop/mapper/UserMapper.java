package com.ecommerce.sshop.mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.model.role.Role;
import com.ecommerce.sshop.model.user.User;

@Mapper(componentModel = "spring", uses = { OrderMapper.class, CartMapper.class })
public interface UserMapper {
    UserDto toDto(User user);

    default Set<String> map(Collection<Role> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
