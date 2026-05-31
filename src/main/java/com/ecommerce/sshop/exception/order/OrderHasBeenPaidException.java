package com.ecommerce.sshop.exception.order;

public class OrderHasBeenPaidException extends RuntimeException {
    public OrderHasBeenPaidException(String message) {
        super(message);
    }
}
