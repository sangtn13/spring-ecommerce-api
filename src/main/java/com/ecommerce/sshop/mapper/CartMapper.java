package com.ecommerce.sshop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.sshop.dto.carts.CartDto;
import com.ecommerce.sshop.model.carts.Cart;

@Mapper(componentModel = "spring", uses = { CartItemMapper.class })
public interface CartMapper {
    @Mapping(target = "cartId", source = "id")
    CartDto toDto(Cart cart);
}
