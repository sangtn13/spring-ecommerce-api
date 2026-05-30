package com.ecommerce.sshop.controller.cart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import com.ecommerce.sshop.controller.carts.CartController;
import com.ecommerce.sshop.dto.carts.CartDto;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.cart.ICartService;
import com.ecommerce.sshop.service.user.IUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private ICartService cartService;
    @Mock
    private IUserService userService;

    @InjectMocks
    private CartController cartController;

    private User mockUser;
    private Cart mockCart;
    private final String userId = "user-123";

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(userId);
        mockCart = new Cart();
    }

    @Test
    @DisplayName("Get cart successfully")
    void getCart_Success() {
        CartDto mockDto = new CartDto();
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartService.getCartByUserId(userId)).thenReturn(mockCart);
        when(cartService.convertToDto(mockCart)).thenReturn(mockDto);

        ResponseEntity<ApiResponse> response = cartController.getCart();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cart retrieved successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Clear cart successfully")
    void clearCart_Success() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        doNothing().when(cartService).clearCartByUserId(userId);

        ResponseEntity<ApiResponse> response = cartController.clearCart();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cart cleared successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Get total amount of current cart successfully")
    void getTotalAmount_Success() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartService.getTotalPriceByUserId(userId)).thenReturn(new BigDecimal("999.00"));

        ResponseEntity<ApiResponse> response = cartController.getTotalAmount();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new BigDecimal("999.00"), response.getBody().getData());
    }
}