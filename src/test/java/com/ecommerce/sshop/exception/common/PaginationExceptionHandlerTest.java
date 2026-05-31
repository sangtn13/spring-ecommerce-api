package com.ecommerce.sshop.exception.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecommerce.sshop.response.ApiResponse;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PaginationExceptionHandlerTest {

    private final PaginationExceptionHandler handler = new PaginationExceptionHandler();

    @Test
    void handlesIllegalArgumentException() {
        ResponseEntity<ApiResponse> response = handler.handleIllegalArgumentException(new IllegalArgumentException("page must be >= 0"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request parameters: page must be >= 0", response.getBody().getMessage());
    }
}

