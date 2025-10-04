package com.ecommerce.sshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.sshop.model.Order;
import java.util.List;

public interface IOrderRepository extends JpaRepository<Order, Long> 
{
    List<Order> findByUserId(Long userId);
}
