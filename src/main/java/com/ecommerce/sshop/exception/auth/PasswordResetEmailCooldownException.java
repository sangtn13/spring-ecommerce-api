package com.ecommerce.sshop.exception.auth;

import com.ecommerce.sshop.exception.common.TooManyRequestsException;

public class PasswordResetEmailCooldownException extends TooManyRequestsException {
    public PasswordResetEmailCooldownException(String message) {
        super(message);
    }
}
