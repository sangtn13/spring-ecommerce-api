package com.ecommerce.sshop.service.order;

import java.util.List;

import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.enums.OrderStatus;
import com.ecommerce.sshop.model.orders.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {
    Order placeOrder(Long userId);

    OrderDto getOrderById(Long orderId);

    List<OrderDto> getUserOrders(Long userId);

    Order updateOrderStatus(Long orderId, OrderStatus status);

    OrderDto convertToDto(Order order);

    Page<OrderDto> getUserOrdersWithPaging(Long userId, Pageable pageable);
}
