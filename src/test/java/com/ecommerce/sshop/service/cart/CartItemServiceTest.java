package com.ecommerce.sshop.service.cart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashSet;

import com.ecommerce.sshop.exception.carts.CartItemNotFoundException;
import com.ecommerce.sshop.exception.carts.QuantityInvalidException;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.carts.CartItem;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.repository.carts.ICartItemRepository;
import com.ecommerce.sshop.repository.carts.ICartRepository;
import com.ecommerce.sshop.service.product.IProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

    @Mock
    private ICartItemRepository cartItemRepository;
    @Mock
    private ICartRepository cartRepository;
    @Mock
    private ICartService cartService;
    @Mock
    private IProductService productService;

    @InjectMocks
    private CartItemService cartItemService;

    private Cart sampleCart;
    private Product sampleProduct;
    private CartItem sampleCartItem;
    private final String cartId = "cart-123";
    private final String productId = "prod-123";

    @BeforeEach
    void setUp() {
        sampleCart = new Cart();
        sampleCart.setId(cartId);
        sampleCart.setItems(new HashSet<>());

        sampleProduct = new Product();
        sampleProduct.setId(productId);
        sampleProduct.setPrice(new BigDecimal("200.00"));

        sampleCartItem = new CartItem();
        sampleCartItem.setId("item-123");
        sampleCartItem.setProduct(sampleProduct);
        sampleCartItem.setQuantity(1);
        sampleCartItem.setUnitPrice(sampleProduct.getPrice());
    }

    @Test
    @DisplayName("Add item to cart successfully")
    void addItemToCart_Success() {
        when(cartService.getCart(cartId)).thenReturn(sampleCart);
        when(productService.getProductById(productId)).thenReturn(sampleProduct);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(sampleCartItem);

        cartItemService.addItemToCart(cartId, productId, 3);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Add item to cart failed - Invalid quantity (<= 0)")
    void addItemToCart_InvalidQuantity_ThrowsException() {
        when(cartService.getCart(cartId)).thenReturn(sampleCart);
        when(productService.getProductById(productId)).thenReturn(sampleProduct);

        assertThrows(QuantityInvalidException.class, () -> cartItemService.addItemToCart(cartId, productId, -1));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Remove item from cart successfully")
    void removeItemFromCart_Success() {
        sampleCart.addItem(sampleCartItem);
        when(cartService.getCart(cartId)).thenReturn(sampleCart);
        when(cartRepository.save(any(Cart.class))).thenReturn(sampleCart);

        cartItemService.removeItemFromCart(cartId, productId);

        assertTrue(sampleCart.getItems().isEmpty());
        verify(cartRepository).save(sampleCart);
    }

    @Test
    @DisplayName("Get CartItem failed - Not found, throws CartItemNotFoundException")
    void getCartItem_NotFound_ThrowsException() {
        when(cartService.getCart(cartId)).thenReturn(sampleCart);

        assertThrows(CartItemNotFoundException.class, () -> cartItemService.getCartItem(cartId, productId));
    }
}