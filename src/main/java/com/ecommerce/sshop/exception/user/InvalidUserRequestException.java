package com.ecommerce.sshop.exception.user;

public class InvalidUserRequestException extends RuntimeException {
    public InvalidUserRequestException(String message) {
        super(message);
    }
}
