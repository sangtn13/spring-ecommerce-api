package com.ecommerce.sshop.service.order;

import com.ecommerce.sshop.dto.OrderDto;
import com.ecommerce.sshop.model.Order;
import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    OrderDto getOrderById(Long orderId);
    List<OrderDto> getUserOrders(Long userId);
}
