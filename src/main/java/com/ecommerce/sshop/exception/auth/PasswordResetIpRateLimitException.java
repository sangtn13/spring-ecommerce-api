package com.ecommerce.sshop.exception.auth;

import com.ecommerce.sshop.exception.common.TooManyRequestsException;

public class PasswordResetIpRateLimitException extends TooManyRequestsException {
    public PasswordResetIpRateLimitException(String message) {
        super(message);
    }
}
