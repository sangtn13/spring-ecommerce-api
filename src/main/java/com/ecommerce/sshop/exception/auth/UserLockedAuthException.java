package com.ecommerce.sshop.exception.auth;

public class UserLockedAuthException extends RuntimeException {
    public UserLockedAuthException(String message) {
        super(message);
    }
}
