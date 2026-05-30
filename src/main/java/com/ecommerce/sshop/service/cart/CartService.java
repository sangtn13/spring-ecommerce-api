package com.ecommerce.sshop.service.cart;

import java.math.BigDecimal;
import java.util.Optional;

import com.ecommerce.sshop.exception.carts.CartNotFoundException;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.dto.carts.CartDto;
import com.ecommerce.sshop.repository.carts.ICartRepository;
import com.ecommerce.sshop.repository.carts.ICartItemRepository;

import lombok.RequiredArgsConstructor;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    private final ICartRepository cartRepository;
    private final ICartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;

    @Override
    public Cart getCart(String id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new CartNotFoundException("Cart with id " + id + " not found"));
        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        return cartRepository.save(cart);
    }

    @Transactional
    @Override
    public void clearCart(String id) {
        Cart cart = getCart(id);
        cartItemRepository.deleteAllByCartId(id);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.deleteById(id);
    }

    @Override
    public BigDecimal getTotalPrice(String id) {
        Cart cart = getCart(id);
        return cart.getTotalAmount();
    }

    @Override
    public Cart initializeNewCart(User user) {
        return Optional.ofNullable(getCartByUserId(user.getId()))
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalAmount(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void clearCartByUserId(String userId) {
        Cart cart = getCartByUserId(userId);
        String cartId = cart.getId();
        cartItemRepository.deleteAllByCartId(cartId);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.deleteById(cartId);
    }

    @Override
    public BigDecimal getTotalPriceByUserId(String userId) {
        Cart cart = getCartByUserId(userId);
        return cart.getTotalAmount();
    }

    @Override
    public CartDto convertToDto(Cart cart) {
        return modelMapper.map(cart, CartDto.class);
    }
}