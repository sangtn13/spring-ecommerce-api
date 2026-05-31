package com.ecommerce.sshop.exception.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecommerce.sshop.exception.carts.EmptyCartException;
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
    void handlesBadRequestGroup() {
        ResponseEntity<ApiResponse> response = handler.handleBadRequestException(new EmptyCartException("bad req"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad req", response.getBody().getMessage());
    }

    @Test
    void handlesNotFoundGroup() {
        ResponseEntity<ApiResponse> response = handler.handleNotFoundException(new UserNotFoundException("not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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

