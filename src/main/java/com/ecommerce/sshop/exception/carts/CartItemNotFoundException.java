package com.ecommerce.sshop.exception.carts;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(String message) {
        super(message);
    }

}
