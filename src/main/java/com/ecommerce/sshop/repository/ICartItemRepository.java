package com.ecommerce.sshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.sshop.model.CartItem;

public interface ICartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteAllByCartId(Long id);
}
