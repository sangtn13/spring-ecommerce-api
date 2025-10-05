package com.ecommerce.sshop.repository.carts;

import com.ecommerce.sshop.model.carts.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ICartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteAllByCartId(Long id);
}
