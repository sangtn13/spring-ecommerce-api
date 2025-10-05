package com.ecommerce.sshop.controller.carts;

import com.ecommerce.sshop.service.cart.ICartItemService;
import com.ecommerce.sshop.service.cart.ICartService;
import com.ecommerce.sshop.service.user.IUserService;
import com.ecommerce.sshop.exception.carts.CartItemNotFoundException;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.response.ApiResponse;

import io.jsonwebtoken.JwtException;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cart-items")
public class CartItemController {
    private final ICartItemService cartItemService;
    private final ICartService cartService;
    private final IUserService userService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addItemToCart(@RequestParam Long productId,
            @RequestParam Integer quantity) {
        try {

            User user = userService.getCurrentUser();
            Cart cart = cartService.initializeNewCart(user);
            cartItemService.addItemToCart(cart.getId(), productId, quantity);
            return ResponseEntity.ok(new ApiResponse("Item added to cart successfully", null));
        } catch (CartItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Cart item not found", null));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Unauthorized: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{cartId}/remove/{itemId}")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable Long cartId, @PathVariable Long itemId) {
        try {
            cartItemService.removeItemFromCart(cartId, itemId);
            return ResponseEntity.ok(new ApiResponse("Item removed from cart successfully", null));
        } catch (CartItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Cart item not found", null));
        }
    }

    @PutMapping("/{cartId}/update/{itemId}")
    public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable Long cartId, @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        try {
            cartItemService.updateItemQuantity(cartId, itemId, quantity);
            return ResponseEntity.ok(new ApiResponse("Item quantity updated successfully", null));
        } catch (CartItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Cart item not found", null));
        }
    }
}
