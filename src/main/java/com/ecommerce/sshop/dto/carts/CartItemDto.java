package com.ecommerce.sshop.dto.carts;

import java.math.BigDecimal;

import com.ecommerce.sshop.dto.product.ProductDto;

import lombok.Data;

@Data
public class CartItemDto {
    private String itemId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private ProductDto product;
}
