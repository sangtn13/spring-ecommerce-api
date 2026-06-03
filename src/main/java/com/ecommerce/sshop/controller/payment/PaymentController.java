package com.ecommerce.sshop.controller.payment;

import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.payment.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/orders/{orderId}/checkout-link")
    public ResponseEntity<ApiResponse> createPaymentLink(@PathVariable String orderId) {
        String checkoutUrl = paymentService.createOrderPaymentLink(orderId);
        return ResponseEntity.ok(new ApiResponse("Created payment link successfully!", checkoutUrl));
    }

    @PostMapping("/payos-webhook")
    public ResponseEntity<ApiResponse> receivePayOSWebhook(@RequestBody Object body) {
        paymentService.handlePayOSWebhook(body);
        return ResponseEntity.ok(new ApiResponse("System has processed the Webhook successfully!", "Success"));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<ApiResponse> markPaymentCanceled(@PathVariable String orderId) {
        paymentService.markPaymentCanceled(orderId);
        return ResponseEntity.ok(new ApiResponse("Payment marked as canceled successfully!", "Canceled"));
    }
}
