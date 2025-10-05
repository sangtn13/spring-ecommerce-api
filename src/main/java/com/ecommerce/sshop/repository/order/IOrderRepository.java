package com.ecommerce.sshop.repository.order;

import java.util.List;

import com.ecommerce.sshop.model.orders.Order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
}
