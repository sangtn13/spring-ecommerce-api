package com.ecommerce.sshop.dto.user;

import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;

import com.ecommerce.sshop.dto.carts.CartDto;
import com.ecommerce.sshop.dto.orders.OrderDto;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean accountLocked;
    private LocalDateTime lastLoginAt;
    private Set<String> roles;
    private List<OrderDto> orders;
    private CartDto cart;
}
