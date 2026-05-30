package com.ecommerce.sshop.service.cart;

import java.math.BigDecimal;

import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.dto.carts.CartDto;

public interface ICartService {
    Cart getCart(String id);

    void clearCart(String id);

    BigDecimal getTotalPrice(String id);

    Cart initializeNewCart(User user);

    Cart getCartByUserId(String userId);

    CartDto convertToDto(Cart cart);

    void clearCartByUserId(String userId);

    BigDecimal getTotalPriceByUserId(String userId);
}
