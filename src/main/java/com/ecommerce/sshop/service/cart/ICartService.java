package com.ecommerce.sshop.service.cart;

import java.math.BigDecimal;

import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.dto.carts.CartDto;

public interface ICartService {
    Cart getCart(Long id);

    void clearCart(Long id);

    BigDecimal getTotalPrice(Long id);

    Cart initializeNewCart(User user);

    Cart getCartByUserId(Long userId);

    CartDto convertToDto(Cart cart);
}
