package com.ecommerce.sshop.controller.order;

import java.util.List;

import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.exception.carts.EmptyCartException;
import com.ecommerce.sshop.exception.order.InsufficientStockException;
import com.ecommerce.sshop.model.orders.Order;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.order.IOrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;

    @PostMapping()
    public ResponseEntity<ApiResponse> createOrder(@RequestParam Long userId) {
        try {
            Order order = orderService.placeOrder(userId);
            OrderDto orderDto = orderService.convertToDto(order);
            return ResponseEntity.ok(new ApiResponse("Order placed successfully", orderDto));
        } catch (EmptyCartException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Error placing order: " + e.getMessage(), null));
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse("Error placing order: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable long orderId) {
        OrderDto order = orderService.getOrderById(orderId);
        if (order != null) {
            return ResponseEntity.ok(new ApiResponse("Order retrieved successfully", order));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Order not found", null));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserOrders(@PathVariable Long userId) {
        List<OrderDto> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully", orders));
    }
}
