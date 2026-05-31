package com.ecommerce.sshop.service.payment;

import com.ecommerce.sshop.enums.OrderStatus;
import com.ecommerce.sshop.enums.PaymentProvider;
import com.ecommerce.sshop.enums.PaymentStatus;
import com.ecommerce.sshop.exception.order.OrderNotFoundException;
import com.ecommerce.sshop.exception.order.OrderNotPendingException;
import com.ecommerce.sshop.model.orders.Order;
import com.ecommerce.sshop.model.payment.Payment;
import com.ecommerce.sshop.repository.order.IOrderRepository;
import com.ecommerce.sshop.repository.payment.IPaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IOrderRepository orderRepository;
    private final IPaymentRepository paymentRepository;
    private final PayOS payOS;
    private final ObjectMapper objectMapper;

    @Value("${payos.return-url-base}")
    private String returnUrlBase;

    @Value("${payos.cancel-url-base}")
    private String cancelUrlBase;

    @Override
    @Transactional
    public String createOrderPaymentLink(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Not found order with ID: " + orderId));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new OrderNotPendingException("Order is not in PENDING status.");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment != null) {
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                throw new IllegalStateException("Order has already been paid successfully. No need to create a new payment link.");
            }
            if (payment.getResponseData() != null) {
                try {
                    CreatePaymentLinkResponse cachedResponse = objectMapper.readValue(
                            payment.getResponseData(), CreatePaymentLinkResponse.class);
                    return cachedResponse.getCheckoutUrl();
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to parse cached payment response.", e);
                }
            }
        }

        List<PaymentLinkItem> items = new ArrayList<>();
        order.getOrderItems().forEach(orderItem -> {
            items.add(PaymentLinkItem.builder()
                .name(orderItem.getProduct().getName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice().longValue())
                .build());
        });

        String returnUrl = returnUrlBase + "?orderId=" + orderId;
        String cancelUrl = cancelUrlBase + "?orderId=" + orderId;

        long orderCode = Math.abs((long) orderId.hashCode());

        CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
            .orderCode(orderCode)
            .amount(order.getTotalAmount().longValue())
            .description("Thanh toan don hang sshop")
            .items(items)
            .returnUrl(returnUrl)
            .cancelUrl(cancelUrl)
            .build();

        CreatePaymentLinkResponse response;
        try {
            response = payOS.paymentRequests().create(paymentRequest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create payment link.", e);
        }

        if (payment == null) {
            payment = new Payment();
            payment.setOrder(order);
            payment.setProvider(PaymentProvider.PAYOS);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(order.getTotalAmount());
            payment.setOrderCode(orderCode);
            payment.setCreatedAt(LocalDateTime.now());
        }

        try {
            payment.setResponseData(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize payment response.", e);
        }
        paymentRepository.save(payment);

        return response.getCheckoutUrl();
    }

    @Override
    @Transactional
    public void handlePayOSWebhook(Object webhookBody) {
        WebhookData data;
        try {
            data = payOS.webhooks().verify(webhookBody);
        } catch (Exception e) {
            throw new IllegalStateException("Webhook verification failed.", e);
        }
        
        if (data != null) {
            long receivedOrderCode = data.getOrderCode();

            Payment payment = paymentRepository.findByOrderCode(receivedOrderCode).orElse(null);
            if (payment == null) {
                return;
            }

            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
            }

            Order order = payment.getOrder();
            if (order != null && order.getOrderStatus() == OrderStatus.PENDING) {
                order.setOrderStatus(OrderStatus.PROCESSING);
                orderRepository.save(order);
            }

            paymentRepository.save(payment);
        }
    }
}