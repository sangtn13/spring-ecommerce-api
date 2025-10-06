package com.ecommerce.sshop.exception.order;

public class StatusInvalidException extends RuntimeException {
    public StatusInvalidException(String message) {
        super(message);
    }
}
