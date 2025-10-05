package com.ecommerce.sshop.dto.carts;

import java.util.Set;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartDto {
    private Long cartId;
    private BigDecimal totalAmount;
    private Set<CartItemDto> items;
}
