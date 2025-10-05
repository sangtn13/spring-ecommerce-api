package com.ecommerce.sshop.repository.carts;

import com.ecommerce.sshop.model.carts.Cart;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ICartRepository extends JpaRepository<Cart, Long> {
     Cart findByUserId(Long userId);
}
