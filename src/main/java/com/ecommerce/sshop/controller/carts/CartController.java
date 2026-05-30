package com.ecommerce.sshop.controller.carts;

import java.math.BigDecimal;

import com.ecommerce.sshop.dto.carts.CartDto;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.cart.ICartService;
import com.ecommerce.sshop.service.user.IUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
    private final ICartService cartService;
    private final IUserService userService;

    @GetMapping("/my-cart")
    public ResponseEntity<ApiResponse> getCart() {
        User user = userService.getCurrentUser();
        Cart cart = cartService.getCartByUserId(user.getId());
        CartDto cartDto = cartService.convertToDto(cart);
        return ResponseEntity.ok(new ApiResponse("Cart retrieved successfully", cartDto));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart() {
        User user = userService.getCurrentUser();
        cartService.clearCartByUserId(user.getId());
        return ResponseEntity.ok(new ApiResponse("Cart cleared successfully", null));
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse> getTotalAmount() {
        User user = userService.getCurrentUser();
        BigDecimal totalPrice = cartService.getTotalPriceByUserId(user.getId());
        return ResponseEntity.ok(new ApiResponse("Total amount retrieved successfully", totalPrice));
    }

}
