package com.ecommerce.sshop.service.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import com.ecommerce.sshop.enums.OrderStatus;
import com.ecommerce.sshop.exception.carts.EmptyCartException;
import com.ecommerce.sshop.exception.order.InsufficientStockException;
import com.ecommerce.sshop.exception.order.OrderNotFoundException;
import com.ecommerce.sshop.model.carts.Cart;
import com.ecommerce.sshop.model.orders.Order;
import com.ecommerce.sshop.repository.order.IOrderRepository;
import com.ecommerce.sshop.repository.product.IProductRepository;
import com.ecommerce.sshop.service.cart.ICartService;
import com.ecommerce.sshop.model.orders.OrderItem;
import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.model.product.Product;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final IOrderRepository orderRepository;
    private final IProductRepository productRepository;
    private final ICartService cartService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot place order with an empty cart");
        }

        validateStock(cart);

        Order order = createOrder(cart);
        List<OrderItem> orderItems = createOrderItems(order, cart);
        BigDecimal totalAmount = calculateTotalAmount(orderItems);
        order.setOrderItems(new HashSet<>(orderItems));
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(cart.getId());
        return savedOrder;
    }

    private void validateStock(Cart cart) {
        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();
            if (product.getInventory() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                                product.getName(),
                                product.getInventory(),
                                cartItem.getQuantity()));
            }
        });
    }

    private Order createOrder(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());
        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        return cart.getItems().stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();
                    product.setInventory(product.getInventory() - cartItem.getQuantity());
                    productRepository.save(product);
                    return new OrderItem(order, product, cartItem.getQuantity(), cartItem.getUnitPrice());
                }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDto)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }

}
