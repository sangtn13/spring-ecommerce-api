package com.ecommerce.sshop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.sshop.dto.orders.OrderItemDto;
import com.ecommerce.sshop.model.orders.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productBrand", source = "product.brand")
    OrderItemDto toDto(OrderItem orderItem);
}
