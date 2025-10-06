package com.ecommerce.sshop.controller.order;

import com.ecommerce.sshop.enums.OrderStatus;
import com.ecommerce.sshop.exception.order.StatusInvalidException;
import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.model.orders.Order;
import com.ecommerce.sshop.response.PagedResponse;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.order.IOrderService;
import com.ecommerce.sshop.service.user.IUserService;
import com.ecommerce.sshop.util.PageUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;
    private final IUserService userService;

    @PostMapping()
    public ResponseEntity<ApiResponse> createOrder() {
        Long userId = userService.getCurrentUser().getId();
        Order order = orderService.placeOrder(userId);
        OrderDto orderDto = orderService.convertToDto(order);
        return ResponseEntity.ok(new ApiResponse("Order placed successfully", orderDto));
    }

    @PreAuthorize("hasAuthority('Admin')")
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

    @GetMapping("my-orders")
    public ResponseEntity<ApiResponse> getUserOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Long userId = userService.getCurrentUser().getId();
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<OrderDto> orders = orderService.getUserOrdersWithPaging(userId, pageable);
        PagedResponse<OrderDto> pagedResponse = PagedResponse.of(orders);
        return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully", pagedResponse));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/{orderId}/status/{status}")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long orderId, @PathVariable String status) {
        if (!EnumUtils.isValidEnum(OrderStatus.class, status.toUpperCase())) {
            throw new StatusInvalidException("Invalid order status: " + status);
        }
        Order order = orderService.updateOrderStatus(orderId,
                Enum.valueOf(OrderStatus.class, status.toUpperCase()));
        OrderDto orderDto = orderService.convertToDto(order);
        return ResponseEntity.ok(new ApiResponse("Order status updated successfully", orderDto));
    }
}
