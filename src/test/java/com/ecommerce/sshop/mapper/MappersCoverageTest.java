package com.ecommerce.sshop.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.ecommerce.sshop.dto.carts.CartDto;
import com.ecommerce.sshop.dto.carts.CartItemDto;
import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.dto.orders.OrderItemDto;
import com.ecommerce.sshop.dto.user.UserDto;
import com.ecommerce.sshop.enums.OrderStatus;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.carts.CartItem;
import com.ecommerce.sshop.model.orders.Order;
import com.ecommerce.sshop.model.orders.OrderItem;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MappersCoverageTest {

    private UserMapper userMapper;
    private CartMapper cartMapper;
    private CartItemMapper cartItemMapper;
    private OrderMapper orderMapper;
    private OrderItemMapper orderItemMapper;
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        // Instantiate all required concrete MapStruct implementations manually
        orderItemMapper = new OrderItemMapperImpl();
        productMapper = new ProductMapperImpl();
        cartItemMapper = new CartItemMapperImpl();
        
        orderMapper = new OrderMapperImpl();
        cartMapper = new CartMapperImpl();
        userMapper = new UserMapperImpl();

        // Bind internal cross-dependencies to resolve nested mapping invocations
        ReflectionTestUtils.setField(orderMapper, "orderItemMapper", orderItemMapper);
        ReflectionTestUtils.setField(cartItemMapper, "productMapper", productMapper);
        ReflectionTestUtils.setField(cartMapper, "cartItemMapper", cartItemMapper);
        ReflectionTestUtils.setField(userMapper, "orderMapper", orderMapper);
        ReflectionTestUtils.setField(userMapper, "cartMapper", cartMapper);
    }

    @Test
    @DisplayName("Check comprehensive mapping of nested structures from User to UserDto")
    void testUserAndNestedMappers() {
        // Initialize a complex User entity with nested Cart, CartItems, Orders, and OrderItems
        Product product = new Product();
        product.setId("prod-999");
        product.setName("Gaming Mouse");
        product.setBrand("Logitech");
        product.setPrice(new BigDecimal("50.00"));

        CartItem cartItem = new CartItem();
        cartItem.setId("item-777");
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(product.getPrice());
        cartItem.setProduct(product);

        Cart cart = new Cart();
        cart.setId("cart-555");
        cart.setTotalAmount(new BigDecimal("100.00"));
        cart.setItems(new HashSet<>(Set.of(cartItem)));

        User user = new User();
        user.setId("user-000");
        user.setEmail("test@gmail.com");
        user.setCart(cart);

        Order order = new Order();
        order.setId("order-111");
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setUser(user);

        OrderItem orderItem = new OrderItem(order, product, 2, product.getPrice());
        orderItem.setId("order-item-222");
        order.setOrderItems(new HashSet<>(Set.of(orderItem)));
        user.setOrders(List.of(order));

        // 1. Map entities to DTOs covering all branches of execution
        UserDto userDto = userMapper.toDto(user);
        OrderDto orderDto = orderMapper.toDto(order);
        CartDto cartDto = cartMapper.toDto(cart);
        CartItemDto cartItemDto = cartItemMapper.toDto(cartItem);
        OrderItemDto orderItemDto = orderItemMapper.toDto(orderItem);

        // 2. Validate all fields are accurately mapped
        assertNotNull(userDto);
        assertEquals("test@gmail.com", userDto.getEmail());
        assertNotNull(userDto.getCart());
        assertEquals("cart-555", userDto.getCart().getCartId());
        
        assertNotNull(orderDto);
        assertEquals("PENDING", orderDto.getStatus());
        assertEquals("user-000", orderDto.getUserId());
        
        assertNotNull(orderItemDto);
        assertEquals("prod-999", orderItemDto.getProductId());
        assertEquals("Gaming Mouse", orderItemDto.getProductName());
        assertEquals("Logitech", orderItemDto.getProductBrand());
    }

    @Test
    @DisplayName("Assert that mappers return null when input is null to cover null-check branches")
    void testMappersWithNullInputs() {
        // Passing null targets MapStruct null-check branches explicitly
        assertNull(userMapper.toDto(null));
        assertNull(cartMapper.toDto(null));
        assertNull(cartItemMapper.toDto(null));
        assertNull(orderMapper.toDto(null));
        assertNull(orderItemMapper.toDto(null));
        assertNull(productMapper.toDto(null));
    }
}