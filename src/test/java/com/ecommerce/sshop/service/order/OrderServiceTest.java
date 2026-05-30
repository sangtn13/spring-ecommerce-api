package com.ecommerce.sshop.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.ecommerce.sshop.enums.OrderStatus;
import com.ecommerce.sshop.exception.carts.EmptyCartException;
import com.ecommerce.sshop.exception.order.InsufficientStockException;
import com.ecommerce.sshop.exception.order.OrderNotFoundException;
import com.ecommerce.sshop.exception.order.StatusInvalidException;
import com.ecommerce.sshop.mapper.OrderMapper;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.carts.CartItem;
import com.ecommerce.sshop.model.orders.Order;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.repository.order.IOrderRepository;
import com.ecommerce.sshop.repository.product.IProductRepository;
import com.ecommerce.sshop.service.cart.ICartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private IOrderRepository orderRepository;
    @Mock private IProductRepository productRepository;
    @Mock private ICartService cartService;
    @Mock private OrderMapper orderMapper;

    @InjectMocks private OrderService orderService;

    private User sampleUser;
    private Cart sampleCart;
    private Product sampleProduct;
    private CartItem sampleCartItem;
    private final String userId = "user-uuid-123";
    private final String orderId = "order-uuid-123";

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(userId);

        sampleProduct = new Product();
        sampleProduct.setId("prod-123");
        sampleProduct.setName("Laptop");
        sampleProduct.setInventory(10);
        sampleProduct.setPrice(new BigDecimal("1000.00"));

        sampleCartItem = new CartItem();
        sampleCartItem.setProduct(sampleProduct);
        sampleCartItem.setQuantity(2);
        sampleCartItem.setUnitPrice(sampleProduct.getPrice());

        sampleCart = new Cart();
        sampleCart.setUser(sampleUser);
        sampleCart.setItems(new HashSet<>(Set.of(sampleCartItem)));
    }

    @Test
    @DisplayName("Place Order successfully with valid cart and sufficient stock")
    void placeOrder_Success() {
        when(cartService.getCartByUserId(userId)).thenReturn(sampleCart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(userId);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        assertEquals(new BigDecimal("2000.00"), result.getTotalAmount());
        assertEquals(8, sampleProduct.getInventory()); // 10 - 2 = 8
        verify(cartService).clearCart(sampleCart.getId());
    }

    @Test
    @DisplayName("Place Order failed - Empty cart, throws EmptyCartException")
    void placeOrder_EmptyCart_ThrowsException() {
        sampleCart.setItems(Collections.emptySet());
        when(cartService.getCartByUserId(userId)).thenReturn(sampleCart);

        assertThrows(EmptyCartException.class, () -> orderService.placeOrder(userId));
    }

    @Test
    @DisplayName("Place Order failed - Insufficient stock, throws InsufficientStockException")
    void placeOrder_InsufficientStock_ThrowsException() {
        sampleCartItem.setQuantity(20); // Vượt quá 10 trong kho
        when(cartService.getCartByUserId(userId)).thenReturn(sampleCart);

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(userId));
    }

    @Test
    @DisplayName("Update Order Status successfully (PENDING -> PROCESSING)")
    void updateOrderStatus_ValidTransition_Success() {
        Order mockOrder = new Order();
        mockOrder.setOrderStatus(OrderStatus.PENDING);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order updated = orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING);

        assertEquals(OrderStatus.PROCESSING, updated.getOrderStatus());
    }

    @Test
    @DisplayName("Update Order Status failed - Invalid transition (DELIVERED -> CANCELED)")
    void updateOrderStatus_InvalidTransition_ThrowsException() {
        Order mockOrder = new Order();
        mockOrder.setOrderStatus(OrderStatus.DELIVERED);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        assertThrows(StatusInvalidException.class, () -> 
            orderService.updateOrderStatus(orderId, OrderStatus.CANCELED)
        );
    }
}