package com.ecommerce.sshop.service.payment;

public interface IPaymentService {
    String createOrderPaymentLink(String orderId);

    void handlePayOSWebhook(Object webhookBody);

    void markPaymentCanceled(String orderId);
}
