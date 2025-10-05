package com.ecommerce.sshop.dto.user;

import java.util.List;

import com.ecommerce.sshop.dto.carts.CartDto;
import com.ecommerce.sshop.dto.orders.OrderDto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<OrderDto> orders;
    private CartDto cart;
}
