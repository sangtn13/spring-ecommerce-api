package com.ecommerce.sshop.dto.orders;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDto> orderItems;
}