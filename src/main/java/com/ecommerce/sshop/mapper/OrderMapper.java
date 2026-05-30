package com.ecommerce.sshop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.model.orders.Order;

@Mapper(componentModel = "spring", uses = { OrderItemMapper.class })
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "status", source = "orderStatus")
    OrderDto toDto(Order order);
}
