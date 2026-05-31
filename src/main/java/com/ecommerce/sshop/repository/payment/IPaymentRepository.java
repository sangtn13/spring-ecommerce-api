package com.ecommerce.sshop.repository.payment;

import java.util.Optional;

import com.ecommerce.sshop.model.payment.Payment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IPaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByOrderCode(Long orderCode);
}
