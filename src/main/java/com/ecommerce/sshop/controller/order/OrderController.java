package com.ecommerce.sshop.controller.order;

import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.model.orders.Order;
import com.ecommerce.sshop.request.orders.UpdateOrderStatusRequest;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
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
        String userId = userService.getCurrentUser().getId();
        Order order = orderService.placeOrder(userId);
        OrderDto orderDto = orderService.convertToDto(order);
        return ResponseEntity.ok(new ApiResponse("Order placed successfully", orderDto));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable String orderId) {
        OrderDto order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(new ApiResponse("Order retrieved successfully", order));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse> getUserOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        String userId = userService.getCurrentUser().getId();
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<OrderDto> orders = orderService.getUserOrdersWithPaging(userId, pageable);
        PagedResponse<OrderDto> pagedResponse = PagedResponse.of(orders);
        return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully", pagedResponse));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable String orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        Order order = orderService.updateOrderStatus(orderId, request == null ? null : request.getStatus());
        OrderDto orderDto = orderService.convertToDto(order);
        return ResponseEntity.ok(new ApiResponse("Order status updated successfully", orderDto));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse> cancelUserOrder(@PathVariable String orderId) {
        String userId = userService.getCurrentUser().getId();
        Order order = orderService.cancelUserOrder(userId, orderId);
        OrderDto orderDto = orderService.convertToDto(order);
        return ResponseEntity.ok(new ApiResponse("Order canceled successfully", orderDto));
    }
}
