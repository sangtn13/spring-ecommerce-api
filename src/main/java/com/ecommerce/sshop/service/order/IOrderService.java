package com.ecommerce.sshop.service.order;

import java.util.List;

import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.enums.OrderStatus;
import com.ecommerce.sshop.model.orders.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {
    Order placeOrder(String userId);

    OrderDto getOrderById(String orderId);

    List<OrderDto> getUserOrders(String userId);

    Order updateOrderStatus(String orderId, OrderStatus status);

    OrderDto convertToDto(Order order);

    Page<OrderDto> getUserOrdersWithPaging(String userId, Pageable pageable);
}
