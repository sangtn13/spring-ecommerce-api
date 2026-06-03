package com.ecommerce.sshop.controller.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import com.ecommerce.sshop.dto.orders.OrderDto;
import com.ecommerce.sshop.exception.order.OrderNotFoundException;
import com.ecommerce.sshop.exception.order.StatusInvalidException;
import com.ecommerce.sshop.model.orders.Order;
import com.ecommerce.sshop.model.user.User;
import com.ecommerce.sshop.request.orders.UpdateOrderStatusRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.order.IOrderService;
import com.ecommerce.sshop.service.user.IUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private IOrderService orderService;
    @Mock
    private IUserService userService;

    @InjectMocks
    private OrderController orderController;

    private User mockUser;
    private Order mockOrder;
    private OrderDto mockOrderDto;
    private final String userId = "user-123";
    private final String orderId = "order-123";

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(userId);
        mockOrder = new Order();
        mockOrderDto = new OrderDto();
    }

    @Test
    @DisplayName("Place order successfully")
    void createOrder_Success() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(orderService.placeOrder(userId)).thenReturn(mockOrder);
        when(orderService.convertToDto(mockOrder)).thenReturn(mockOrderDto);

        ResponseEntity<ApiResponse> response = orderController.createOrder();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order placed successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Get order by ID successfully (Admin)")
    void getOrderById_Success() {
        when(orderService.getOrderById(orderId)).thenReturn(mockOrderDto);

        ResponseEntity<ApiResponse> response = orderController.getOrderById(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockOrderDto, response.getBody().getData());
    }

    @Test
    @DisplayName("Get order by ID fails when order is not found - Throws OrderNotFoundException")
    void getOrderById_NotFound() {
        when(orderService.getOrderById(orderId)).thenThrow(new OrderNotFoundException("Order not found with id: " + orderId));
        assertThrows(OrderNotFoundException.class, () -> orderController.getOrderById(orderId));
    }

    @Test
    @DisplayName("Get user's orders with paging successfully")
    void getUserOrders_Success() {
        Page<OrderDto> page = new PageImpl<>(List.of(mockOrderDto));
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(orderService.getUserOrdersWithPaging(eq(userId), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse> response = orderController.getUserOrders(1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Update order status successfully (Admin)")
    void updateOrderStatus_Success() {
        when(orderService.updateOrderStatus(orderId, "SHIPPED")).thenReturn(mockOrder);
        when(orderService.convertToDto(mockOrder)).thenReturn(mockOrderDto);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("SHIPPED");

        ResponseEntity<ApiResponse> response = orderController.updateOrderStatus(orderId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order status updated successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Update order status fails when status is invalid")
    void updateOrderStatus_InvalidStatus_ThrowsException() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("INVALID_STATUS_STRING");

        when(orderService.updateOrderStatus(orderId, "INVALID_STATUS_STRING"))
                .thenThrow(new StatusInvalidException("Invalid order status: INVALID_STATUS_STRING"));

        assertThrows(StatusInvalidException.class, () -> orderController.updateOrderStatus(orderId, request));
        verify(orderService).updateOrderStatus(orderId, "INVALID_STATUS_STRING");
    }

    @Test
    @DisplayName("Cancel user order successfully")
    void cancelUserOrder_Success() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(orderService.cancelUserOrder(userId, orderId)).thenReturn(mockOrder);
        when(orderService.convertToDto(mockOrder)).thenReturn(mockOrderDto);

        ResponseEntity<ApiResponse> response = orderController.cancelUserOrder(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order canceled successfully", response.getBody().getMessage());
        assertEquals(mockOrderDto, response.getBody().getData());
    }
}
