package com.ecommerce.sshop.controller.carts;

import com.ecommerce.sshop.service.cart.ICartItemService;
import com.ecommerce.sshop.service.cart.ICartService;
import com.ecommerce.sshop.service.user.IUserService;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.response.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cart-items")
public class CartItemController {
    private final ICartItemService cartItemService;
    private final ICartService cartService;
    private final IUserService userService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addItemToCart(@RequestParam String productId,
            @RequestParam Integer quantity) {
        User user = userService.getCurrentUser();
        Cart cart = cartService.initializeNewCart(user);
        cartItemService.addItemToCart(cart.getId(), productId, quantity);
        return ResponseEntity.ok(new ApiResponse("Item added to cart successfully", null));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable String itemId) {
        User user = userService.getCurrentUser();
        Cart cart = cartService.getCartByUserId(user.getId());
        cartItemService.removeItemFromCart(cart.getId(), itemId);
        return ResponseEntity.ok(new ApiResponse("Item removed from cart successfully", null));
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable String itemId,
            @RequestParam Integer quantity) {
        User user = userService.getCurrentUser();
        Cart cart = cartService.getCartByUserId(user.getId());
        cartItemService.updateItemQuantity(cart.getId(), itemId, quantity);
        return ResponseEntity.ok(new ApiResponse("Item quantity updated successfully", null));
    }
}
