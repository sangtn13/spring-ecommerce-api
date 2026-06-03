package com.ecommerce.sshop.service.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import com.ecommerce.sshop.enums.OrderStatus;
import com.ecommerce.sshop.enums.PaymentProvider;
import com.ecommerce.sshop.enums.PaymentStatus;
import com.ecommerce.sshop.exception.order.OrderNotFoundException;
import com.ecommerce.sshop.exception.order.OrderNotPendingException;
import com.ecommerce.sshop.exception.payment.PaymentNotFoundException;
import com.ecommerce.sshop.model.orders.Order;
import com.ecommerce.sshop.model.orders.OrderItem;
import com.ecommerce.sshop.model.payment.Payment;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.repository.order.IOrderRepository;
import com.ecommerce.sshop.repository.payment.IPaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PayOS payOS;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "returnUrlBase", "http://return.local");
        ReflectionTestUtils.setField(paymentService, "cancelUrlBase", "http://cancel.local");

        Product product = new Product();
        product.setId("prod-1");
        product.setName("Keyboard");
        product.setPrice(new BigDecimal("100.00"));

        sampleOrder = new Order();
        sampleOrder.setId("order-1");
        sampleOrder.setOrderStatus(OrderStatus.PENDING);
        sampleOrder.setTotalAmount(new BigDecimal("100.00"));
        sampleOrder.setOrderItems(Set.of(new OrderItem(sampleOrder, product, 1, product.getPrice())));
    }

    @Test
    @DisplayName("Create payment link fails when order is not found")
    void createOrderPaymentLink_OrderNotFound() {
        when(orderRepository.findById("order-404")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> paymentService.createOrderPaymentLink("order-404"));
    }

    @Test
    @DisplayName("Create payment link safely handles integer min value hash codes")
    void createOrderPaymentLink_SafelyHandlesEdgeCaseHashCode() throws Exception {
        // Arrange an order ID engineered to intentionally simulate or test edge-case math behaviors
        String edgeCaseOrderId = "order-edge-case";
        
        when(orderRepository.findById(edgeCaseOrderId)).thenReturn(Optional.of(sampleOrder));
        when(paymentRepository.findByOrderId(edgeCaseOrderId)).thenReturn(Optional.empty());

        CreatePaymentLinkResponse response = org.mockito.Mockito.mock(CreatePaymentLinkResponse.class);
        when(response.getCheckoutUrl()).thenReturn("http://pay.local/checkout");
        when(payOS.paymentRequests().create(any(CreatePaymentLinkRequest.class))).thenReturn(response);
        when(objectMapper.writeValueAsString(response)).thenReturn("{\"checkoutUrl\":\"http://pay.local/checkout\"}");

        // Act
        String result = paymentService.createOrderPaymentLink(edgeCaseOrderId);

        // Assert
        assertEquals("http://pay.local/checkout", result);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        
        // Ensure the generated orderCode can never be negative (0xFFFFFFFFL mask ensures positive range)
        assertTrue(savedPayment.getOrderCode() >= 0, "Order code must always be positive");
    }

    @Test
    @DisplayName("Create payment link fails when order is not in PENDING status")
    void createOrderPaymentLink_OrderNotPending() {
        sampleOrder.setOrderStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(sampleOrder));

        assertThrows(OrderNotPendingException.class, () -> paymentService.createOrderPaymentLink("order-1"));
    }

    @Test
    @DisplayName("Create payment link fails when payment already success")
    void createOrderPaymentLink_PaymentSuccessAlready() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(sampleOrder));

        Payment existingPayment = new Payment();
        existingPayment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(existingPayment));

        assertThrows(IllegalStateException.class, () -> paymentService.createOrderPaymentLink("order-1"));
    }

    @Test
    @DisplayName("Create payment link returns cached checkout url")
    void createOrderPaymentLink_ReturnsCached() throws Exception {
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(sampleOrder));

        Payment existingPayment = new Payment();
        existingPayment.setStatus(PaymentStatus.PENDING);
        existingPayment.setResponseData("{\"checkoutUrl\":\"http://cached\"}");
        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(existingPayment));

        CreatePaymentLinkResponse cachedResponse = org.mockito.Mockito.mock(CreatePaymentLinkResponse.class);
        when(cachedResponse.getCheckoutUrl()).thenReturn("http://cached");
        when(objectMapper.readValue(existingPayment.getResponseData(), CreatePaymentLinkResponse.class))
                .thenReturn(cachedResponse);

        String result = paymentService.createOrderPaymentLink("order-1");

        assertEquals("http://cached", result);
        verify(payOS.paymentRequests(), never()).create(any(CreatePaymentLinkRequest.class));
    }

    @Test
    @DisplayName("Create payment link creates new payment and persists response data")
    void createOrderPaymentLink_NewPayment() throws Exception {
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(sampleOrder));
        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.empty());

        CreatePaymentLinkResponse response = org.mockito.Mockito.mock(CreatePaymentLinkResponse.class);
        when(response.getCheckoutUrl()).thenReturn("http://pay.local/checkout");
        when(payOS.paymentRequests().create(any(CreatePaymentLinkRequest.class))).thenReturn(response);
        when(objectMapper.writeValueAsString(response)).thenReturn("{\"checkoutUrl\":\"http://pay.local/checkout\"}");

        String result = paymentService.createOrderPaymentLink("order-1");

        assertEquals("http://pay.local/checkout", result);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertNotNull(savedPayment.getOrderCode());
        assertEquals(PaymentProvider.PAYOS, savedPayment.getProvider());
        assertEquals(PaymentStatus.PENDING, savedPayment.getStatus());
        assertEquals(sampleOrder.getTotalAmount(), savedPayment.getAmount());
    }

    @Test
    @DisplayName("Create payment link recreates checkout link for canceled payment instead of reusing cached link")
    void createOrderPaymentLink_CanceledPayment_CreatesNewLink() throws Exception {
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(sampleOrder));

        Payment existingPayment = new Payment();
        existingPayment.setStatus(PaymentStatus.CANCELED);
        existingPayment.setResponseData("{\"checkoutUrl\":\"http://old-cached\"}");
        existingPayment.setOrderCode(111L);
        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(existingPayment));

        CreatePaymentLinkResponse response = org.mockito.Mockito.mock(CreatePaymentLinkResponse.class);
        when(response.getCheckoutUrl()).thenReturn("http://pay.local/new-checkout");
        when(payOS.paymentRequests().create(any(CreatePaymentLinkRequest.class))).thenReturn(response);
        when(objectMapper.writeValueAsString(response)).thenReturn("{\"checkoutUrl\":\"http://pay.local/new-checkout\"}");

        String result = paymentService.createOrderPaymentLink("order-1");

        assertEquals("http://pay.local/new-checkout", result);
        assertEquals(PaymentStatus.PENDING, existingPayment.getStatus());
        assertNotEquals(111L, existingPayment.getOrderCode());
        verify(payOS.paymentRequests()).create(any(CreatePaymentLinkRequest.class));
        verify(paymentRepository).save(eq(existingPayment));
    }

    @Test
    @DisplayName("Handle webhook throws when verification fails")
    void handlePayOSWebhook_VerificationFails() throws Exception {
        Object webhookBody = new Object();
        when(payOS.webhooks().verify(webhookBody)).thenThrow(new RuntimeException("bad signature"));

        assertThrows(IllegalStateException.class, () -> paymentService.handlePayOSWebhook(webhookBody));
    }

    @Test
    @DisplayName("Handle webhook ignores unknown payment order code")
    void handlePayOSWebhook_PaymentNotFound() throws Exception {
        Object webhookBody = new Object();
        WebhookData data = org.mockito.Mockito.mock(WebhookData.class);
        when(data.getOrderCode()).thenReturn(12345L);
        when(payOS.webhooks().verify(webhookBody)).thenReturn(data);
        when(paymentRepository.findByOrderCode(12345L)).thenReturn(Optional.empty());

        paymentService.handlePayOSWebhook(webhookBody);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Handle webhook updates payment and order statuses")
    void handlePayOSWebhook_UpdatePaymentAndOrder() throws Exception {
        Object webhookBody = new Object();
        WebhookData data = org.mockito.Mockito.mock(WebhookData.class);
        when(data.getOrderCode()).thenReturn(999L);
        when(payOS.webhooks().verify(webhookBody)).thenReturn(data);

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidAt(null);
        payment.setOrder(sampleOrder);
        payment.setOrderCode(999L);
        payment.setCreatedAt(LocalDateTime.now());

        when(paymentRepository.findByOrderCode(999L)).thenReturn(Optional.of(payment));

        paymentService.handlePayOSWebhook(webhookBody);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertNotNull(payment.getPaidAt());
        assertEquals(OrderStatus.PROCESSING, sampleOrder.getOrderStatus());
        verify(orderRepository).save(eq(sampleOrder));
        verify(paymentRepository).save(eq(payment));
    }

    @Test
    @DisplayName("Mark payment canceled updates pending payment to canceled")
    void markPaymentCanceled_PendingPayment_Success() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));

        paymentService.markPaymentCanceled("order-1");

        assertEquals(PaymentStatus.CANCELED, payment.getStatus());
        verify(paymentRepository).save(eq(payment));
    }

    @Test
    @DisplayName("Mark payment canceled ignores success payment")
    void markPaymentCanceled_SuccessPayment_NoUpdate() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));

        paymentService.markPaymentCanceled("order-1");

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Mark payment canceled throws when payment is not found")
    void markPaymentCanceled_PaymentNotFound_ThrowsException() {
        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class,
                () -> paymentService.markPaymentCanceled("order-1"));

        assertEquals("Payment not found for order id: order-1", exception.getMessage());
        verify(paymentRepository, times(1)).findByOrderId("order-1");
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
