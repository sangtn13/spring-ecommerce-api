package com.ecommerce.sshop.service.cart;

import java.math.BigDecimal;

import com.ecommerce.sshop.exception.carts.CartItemNotFoundException;
import com.ecommerce.sshop.exception.carts.QuantityInvalidException;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.carts.CartItem;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.repository.carts.ICartItemRepository;
import com.ecommerce.sshop.repository.carts.ICartRepository;
import com.ecommerce.sshop.service.product.IProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService {
    private final ICartItemRepository cartItemRepository;
    private final ICartRepository cartRepository;
    private final ICartService cartService;
    private final IProductService productService;

    @Override
    public void addItemToCart(Long cartId, Long productId, int quantity) {
        Cart cart = cartService.getCart(cartId);
        Product product = productService.getProductById(productId);
        CartItem cartItem = cart.getItems().stream().filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().orElse(new CartItem());
        if (quantity <= 0) {
            throw new QuantityInvalidException("Quantity must be greater than zero");
        }
        if (cartItem.getId() == null) {
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setCart(cart);
            cartItem.setUnitPrice(product.getPrice());
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }
        cartItem.setTotalPrice();
        cart.addItem(cartItem);
        cartItemRepository.save(cartItem);
    }

    @Override
    public void removeItemFromCart(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        CartItem cartItem = getCartItem(cartId, productId);
        if (cartItem != null) {
            cart.removeItem(cartItem);
            cartRepository.save(cart);
        }
    }

    @Override
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        Cart cart = cartService.getCart(cartId);
        cart.getItems().stream().filter(item -> item.getProduct().getId().equals(productId)).findFirst()
                .ifPresent(cartItem -> {
                    cartItem.setQuantity(quantity);
                    cartItem.setUnitPrice(cartItem.getProduct().getPrice());
                    cartItem.setTotalPrice();
                });
        BigDecimal totalAmount = cart.getItems().stream().map(CartItem::getTotalPrice).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
    }

    @Override
    public CartItem getCartItem(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        return cart.getItems().stream().filter(item -> item.getProduct().getId().equals(productId)).findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));
    }

}
