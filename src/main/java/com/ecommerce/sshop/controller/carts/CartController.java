package com.ecommerce.sshop.controller.carts;

import java.math.BigDecimal;

import com.ecommerce.sshop.dto.carts.CartDto;
import com.ecommerce.sshop.exception.carts.CartNotFoundException;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.cart.ICartService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
    private final ICartService cartService;

    @GetMapping("/{cartId}")
    public ResponseEntity<ApiResponse> getCart(@PathVariable Long cartId) {
        try {
            Cart cart = cartService.getCart(cartId);
            CartDto cartDto = cartService.convertToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Cart retrieved successfully", cartDto));
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Cart not found", null));
        }
    }

    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable Long cartId) {
        try {
            cartService.clearCart(cartId);
            return ResponseEntity.ok(new ApiResponse("Cart cleared successfully", null));
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Cart not found", null));
        }
    }

    @GetMapping("/{cartId}/total")
    public ResponseEntity<ApiResponse> getTotalAmount(@PathVariable Long cartId) {
        try {
            BigDecimal totalPrice = cartService.getTotalPrice(cartId);
            return ResponseEntity.ok(new ApiResponse("Total amount retrieved successfully", totalPrice));
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Cart not found", null));
        }
    }

}
