package com.ecommerce.sshop.repository.order;

import java.util.List;

import com.ecommerce.sshop.model.orders.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserId(String userId);

    Page<Order> findByUserId(String userId, Pageable pageable);
}
