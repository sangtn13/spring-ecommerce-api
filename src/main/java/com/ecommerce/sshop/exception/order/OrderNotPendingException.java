package com.ecommerce.sshop.exception.order;

public class OrderNotPendingException extends RuntimeException {
    public OrderNotPendingException(String message) {
        super(message);
    }
}
