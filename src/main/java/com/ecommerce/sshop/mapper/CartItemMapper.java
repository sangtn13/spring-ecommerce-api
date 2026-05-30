package com.ecommerce.sshop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.sshop.dto.carts.CartItemDto;
import com.ecommerce.sshop.model.carts.CartItem;

@Mapper(componentModel = "spring", uses = { ProductMapper.class })
public interface CartItemMapper {
    @Mapping(target = "itemId", source = "id")
    CartItemDto toDto(CartItem cartItem);
}
