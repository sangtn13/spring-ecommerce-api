package com.ecommerce.sshop.service.cart;

import com.ecommerce.sshop.model.carts.CartItem;

public interface ICartItemService {
    void addItemToCart(String cartId, String productId, int quantity);

    void removeItemFromCart(String cartId, String productId);

    void updateItemQuantity(String cartId, String productId, int quantity);

    CartItem getCartItem(String cartId, String productId);
}
