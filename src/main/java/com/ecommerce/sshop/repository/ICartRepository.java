package com.ecommerce.sshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sshop.model.Cart;

public interface ICartRepository extends JpaRepository<Cart, Long> {
     Cart findByUserId(Long userId);
}
