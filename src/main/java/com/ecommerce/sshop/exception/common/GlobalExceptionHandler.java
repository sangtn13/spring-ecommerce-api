package com.ecommerce.sshop.exception.common;

import com.ecommerce.sshop.exception.carts.CartNotFoundException;
import com.ecommerce.sshop.exception.carts.QuantityInvalidException;
import com.ecommerce.sshop.exception.category.CategoryNotFoundException;
import com.ecommerce.sshop.exception.image.ImageNotFoundException;
import com.ecommerce.sshop.exception.order.OrderNotFoundException;
import com.ecommerce.sshop.exception.product.ProductNotFoundException;
import com.ecommerce.sshop.exception.user.UserNotFoundException;
import com.ecommerce.sshop.response.ApiResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String message = "You do not have permission to access this resource.";
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(message, null));
    }

    @ExceptionHandler({
            QuantityInvalidException.class,
    })
    public ResponseEntity<ApiResponse> handleBadRequestException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), null));
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            CartNotFoundException.class,
            ProductNotFoundException.class,
            CategoryNotFoundException.class,
            OrderNotFoundException.class,
            ImageNotFoundException.class
    })
    public ResponseEntity<ApiResponse> handleNotFoundException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(ex.getMessage(), null));
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleAlreadyExistsException(AlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("An error occurred: " + e.getMessage(), null));
    }
}
