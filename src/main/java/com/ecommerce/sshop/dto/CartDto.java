package com.ecommerce.sshop.dto;

import java.util.Set;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartDto {
    private Long cartId;
    private BigDecimal totalAmount;
    private Set<CartItemDto> items;
}
