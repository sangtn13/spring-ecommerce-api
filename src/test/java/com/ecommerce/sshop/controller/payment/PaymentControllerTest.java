package com.ecommerce.sshop.controller.payment;

import com.ecommerce.sshop.exception.order.OrderNotFoundException;
import com.ecommerce.sshop.exception.order.OrderNotPendingException;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.payment.IPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private IPaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private String orderId;
    private String mockCheckoutUrl;

    @BeforeEach
    void setUp() {
        orderId = "order_abc123";
        mockCheckoutUrl = "https://checkout.payos.vn/order/v/mockUrl";
    }

    // =========================================================================
    // TEST FOR: createPaymentLink(String orderId)
    // =========================================================================

    @Test
    void createPaymentLink_Success_ShouldReturnOk() {
        // Given
        Mockito.when(paymentService.createOrderPaymentLink(eq(orderId)))
                .thenReturn(mockCheckoutUrl);

        // When
        ResponseEntity<ApiResponse> responseEntity = paymentController.createPaymentLink(orderId);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Created payment link successfully!", responseEntity.getBody().getMessage());
        assertEquals(mockCheckoutUrl, responseEntity.getBody().getData());

        Mockito.verify(paymentService, Mockito.times(1)).createOrderPaymentLink(eq(orderId));
    }

    @Test
    void createPaymentLink_OrderNotFound_ShouldThrowOrderNotFoundException() {
        // Given
        Mockito.when(paymentService.createOrderPaymentLink(eq(orderId)))
                .thenThrow(new OrderNotFoundException("Not found order with ID: " + orderId));

        // When & Then
        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
            paymentController.createPaymentLink(orderId);
        });

        assertEquals("Not found order with ID: " + orderId, exception.getMessage());
        Mockito.verify(paymentService, Mockito.times(1)).createOrderPaymentLink(eq(orderId));
    }

    @Test
    void createPaymentLink_OrderNotPending_ShouldThrowOrderNotPendingException() {
        // Given
        Mockito.when(paymentService.createOrderPaymentLink(eq(orderId)))
                .thenThrow(new OrderNotPendingException("Order is not in PENDING status."));

        // When & Then
        OrderNotPendingException exception = assertThrows(OrderNotPendingException.class, () -> {
            paymentController.createPaymentLink(orderId);
        });

        assertEquals("Order is not in PENDING status.", exception.getMessage());
        Mockito.verify(paymentService, Mockito.times(1)).createOrderPaymentLink(eq(orderId));
    }

    // =========================================================================
    // TEST FOR: receivePayOSWebhook(Object body)
    // =========================================================================

    @Test
    void receivePayOSWebhook_Success_ShouldReturnOk() {
        // Given
        Object mockWebhookBody = new Object();
        Mockito.doNothing().when(paymentService).handlePayOSWebhook(any());

        // When
        ResponseEntity<ApiResponse> responseEntity = paymentController.receivePayOSWebhook(mockWebhookBody);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("System has processed the Webhook successfully!", responseEntity.getBody().getMessage());
        assertEquals("Success", responseEntity.getBody().getData());

        Mockito.verify(paymentService, Mockito.times(1)).handlePayOSWebhook(any());
    }

    @Test
    void receivePayOSWebhook_VerificationFailed_ShouldThrowIllegalStateException() {
        // Given
        Object mockInvalidBody = new Object();
        Mockito.doThrow(new IllegalStateException("Webhook verification failed."))
                .when(paymentService).handlePayOSWebhook(any());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            paymentController.receivePayOSWebhook(mockInvalidBody);
        });

        assertEquals("Webhook verification failed.", exception.getMessage());
        Mockito.verify(paymentService, Mockito.times(1)).handlePayOSWebhook(any());
    }

    @Test
    void markPaymentCanceled_Success_ShouldReturnOk() {
        Mockito.doNothing().when(paymentService).markPaymentCanceled(eq(orderId));

        ResponseEntity<ApiResponse> responseEntity = paymentController.markPaymentCanceled(orderId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Payment marked as canceled successfully!", responseEntity.getBody().getMessage());
        assertEquals("Canceled", responseEntity.getBody().getData());
        Mockito.verify(paymentService, Mockito.times(1)).markPaymentCanceled(eq(orderId));
    }
}
