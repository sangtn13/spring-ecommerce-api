package com.ecommerce.sshop.service.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import com.ecommerce.sshop.enums.OrderStatus;
import com.ecommerce.sshop.exception.carts.EmptyCartException;
import com.ecommerce.sshop.exception.order.InsufficientStockException;
import com.ecommerce.sshop.exception.order.StatusInvalidException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Order placeOrder(String userId) {
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
    public OrderDto getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDto)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    public List<OrderDto> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        if (!isValidStatusTransition(order.getOrderStatus(), status)) {
            throw new StatusInvalidException(
                    String.format("Invalid status transition from %s to %s", order.getOrderStatus(), status));
        }
        order.setOrderStatus(status);
        return orderRepository.save(order);
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELED;
            case PROCESSING:
                return newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case SHIPPED:
                return newStatus == OrderStatus.DELIVERED;
            case DELIVERED:
            case CANCELED:
                return false; // No transitions allowed from DELIVERED or CANCELED
            default:
                return false;
        }
    }

    @Override
    public OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }

    @Override
    public Page<OrderDto> getUserOrdersWithPaging(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::convertToDto);
    }

}
