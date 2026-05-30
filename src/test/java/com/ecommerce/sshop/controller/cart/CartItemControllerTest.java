package com.ecommerce.sshop.controller.cart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ecommerce.sshop.controller.carts.CartItemController;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.cart.ICartItemService;
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
class CartItemControllerTest {

    @Mock
    private ICartItemService cartItemService;
    @Mock
    private ICartService cartService;
    @Mock
    private IUserService userService;

    @InjectMocks
    private CartItemController cartItemController;

    private User mockUser;
    private Cart mockCart;
    private final String cartId = "cart-123";
    private final String userId = "user-123";

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(userId);
        mockCart = new Cart();
        mockCart.setId(cartId);
    }

    @Test
    @DisplayName("Add item to cart successfully")
    void addItemToCart_Success() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartService.initializeNewCart(mockUser)).thenReturn(mockCart);
        doNothing().when(cartItemService).addItemToCart(cartId, "prod-123", 2);

        ResponseEntity<ApiResponse> response = cartItemController.addItemToCart("prod-123", 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Item added to cart successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Remove item from cart successfully")
    void removeItemFromCart_Success() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartService.getCartByUserId(userId)).thenReturn(mockCart);
        doNothing().when(cartItemService).removeItemFromCart(cartId, "item-123");

        ResponseEntity<ApiResponse> response = cartItemController.removeItemFromCart("item-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Item removed from cart successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Update item quantity in cart successfully")
    void updateItemQuantity_Success() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(cartService.getCartByUserId(userId)).thenReturn(mockCart);
        doNothing().when(cartItemService).updateItemQuantity(cartId, "item-123", 5);

        ResponseEntity<ApiResponse> response = cartItemController.updateItemQuantity("item-123", 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Item quantity updated successfully", response.getBody().getMessage());
    }
}