package com.ecommerce.sshop.service.order;

import java.util.List;

import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.model.orders.Order;

public interface IOrderService {
    Order placeOrder(Long userId);

    OrderDto getOrderById(Long orderId);

    List<OrderDto> getUserOrders(Long userId);

    OrderDto convertToDto(Order order);
}
