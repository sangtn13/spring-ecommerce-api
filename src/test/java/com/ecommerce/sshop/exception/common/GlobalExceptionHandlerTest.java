package com.ecommerce.sshop.exception.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecommerce.sshop.exception.auth.InvalidCredentialsException;
import com.ecommerce.sshop.exception.auth.InvalidPasswordResetTokenException;
import com.ecommerce.sshop.exception.auth.InvalidRefreshTokenException;
import com.ecommerce.sshop.exception.auth.UserLockedAuthException;
import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.exception.carts.CartItemNotFoundException;
import com.ecommerce.sshop.exception.carts.EmptyCartException;
import com.ecommerce.sshop.exception.payment.PaymentNotFoundException;
import com.ecommerce.sshop.exception.user.InvalidUserRequestException;
import com.ecommerce.sshop.exception.user.UserNotFoundException;
import com.ecommerce.sshop.response.ApiResponse;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesAccessDenied() {
        ResponseEntity<ApiResponse> response = handler.handleAccessDeniedException(new AccessDeniedException("x"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handlesUnauthorizedAuthExceptions() {
        ResponseEntity<ApiResponse> invalidCredentials = handler.handleUnauthorizedException(
                new InvalidCredentialsException("Invalid email or password"));
        ResponseEntity<ApiResponse> invalidRefresh = handler.handleUnauthorizedException(
                new InvalidRefreshTokenException("Invalid or expired refresh token"));
        assertEquals(HttpStatus.UNAUTHORIZED, invalidCredentials.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, invalidRefresh.getStatusCode());
    }

    @Test
    void handlesLockedAuthException() {
        ResponseEntity<ApiResponse> response = handler.handleLockedException(new UserLockedAuthException("User is locked"));
        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertEquals("User is locked", response.getBody().getMessage());
    }

    @Test
    void handlesBadRequestGroup() {
        ResponseEntity<ApiResponse> response = handler.handleBadRequestException(new EmptyCartException("bad req"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad req", response.getBody().getMessage());
    }

    @Test
    void handlesInvalidPasswordResetTokenAsBadRequest() {
        ResponseEntity<ApiResponse> response = handler.handleBadRequestException(
                new InvalidPasswordResetTokenException("Invalid or expired password reset token"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired password reset token", response.getBody().getMessage());
    }

    @Test
    void handlesInvalidUserRequestAsBadRequest() {
        ResponseEntity<ApiResponse> response = handler.handleBadRequestException(
                new InvalidUserRequestException("roles must contain only: User, Admin, Manager"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("roles must contain only: User, Admin, Manager", response.getBody().getMessage());
    }

    @Test
    void handlesNotFoundGroup() {
        ResponseEntity<ApiResponse> response = handler.handleNotFoundException(new UserNotFoundException("not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handlesCartItemNotFoundAsNotFound() {
        ResponseEntity<ApiResponse> response = handler.handleNotFoundException(
                new CartItemNotFoundException("Cart item not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Cart item not found", response.getBody().getMessage());
    }

    @Test
    void handlesPaymentNotFoundAsNotFound() {
        ResponseEntity<ApiResponse> response = handler.handleNotFoundException(
                new PaymentNotFoundException("Payment not found for order id: order-1"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Payment not found for order id: order-1", response.getBody().getMessage());
    }

    @Test
    void handlesAlreadyExists() {
        ResponseEntity<ApiResponse> response = handler.handleAlreadyExistsException(new AlreadyExistsException("exists"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handlesGenericException() {
        ResponseEntity<ApiResponse> response = handler.handleGenericException(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
