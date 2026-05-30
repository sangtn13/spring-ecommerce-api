package com.ecommerce.sshop.service.cart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import com.ecommerce.sshop.exception.carts.CartNotFoundException;
import com.ecommerce.sshop.exception.carts.EmptyCartException;
import com.ecommerce.sshop.mapper.CartMapper;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.carts.ICartItemRepository;
import com.ecommerce.sshop.repository.carts.ICartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ICartRepository cartRepository;
    @Mock
    private ICartItemRepository cartItemRepository;
    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private Cart sampleCart;
    private User sampleUser;
    private final String cartId = "cart-123";
    private final String userId = "user-123";

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(userId);

        sampleCart = new Cart();
        sampleCart.setId(cartId);
        sampleCart.setUser(sampleUser);
        sampleCart.setItems(new HashSet<>());
        sampleCart.setTotalAmount(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Get Cart successfully by ID")
    void getCart_Success() {
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(sampleCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(sampleCart);

        Cart result = cartService.getCart(cartId);

        assertNotNull(result);
        assertEquals(cartId, result.getId());
        verify(cartRepository).findById(cartId);
    }

    @Test
    @DisplayName("Get Cart failed - ID not found, throws CartNotFoundException")
    void getCart_NotFound_ThrowsException() {
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.getCart(cartId));
    }

    @Test
    @DisplayName("Initialize new cart successfully if it doesn't exist")
    void initializeNewCart_CreateNew_Success() {
        when(cartRepository.findByUserId(userId)).thenReturn(null);
        when(cartRepository.save(any(Cart.class))).thenReturn(sampleCart);

        Cart result = cartService.initializeNewCart(sampleUser);

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Clear cart successfully by User ID")
    void clearCartByUserId_Success() {
        when(cartRepository.findByUserId(userId)).thenReturn(sampleCart);
        doNothing().when(cartItemRepository).deleteAllByCartId(cartId);
        doNothing().when(cartRepository).deleteById(cartId);

        assertDoesNotThrow(() -> cartService.clearCartByUserId(userId));

        verify(cartItemRepository).deleteAllByCartId(cartId);
        verify(cartRepository).deleteById(cartId);
    }

    @Test
    @DisplayName("Get Cart by User ID failed when cart is empty or not created")
    void getCartByUserId_Empty_ThrowsException() {
        when(cartRepository.findByUserId(userId)).thenReturn(null);

        assertThrows(EmptyCartException.class, () -> cartService.getCartByUserId(userId));
    }
}