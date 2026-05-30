package com.ecommerce.sshop.dto.orders;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemDto {
    private String productId;
    private String productName;
    private String productBrand;
    private int quantity;
    private BigDecimal price;
}
