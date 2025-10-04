package com.ecommerce.sshop.service.cart;

import java.math.BigDecimal;

import com.ecommerce.sshop.model.Cart;

public interface ICartService {
    Cart getCart(Long id);

    void clearCart(Long id);

    BigDecimal getTotalPrice(Long id);

    Long initializeNewCart();

    Cart getCartByUserId(Long userId);
}
