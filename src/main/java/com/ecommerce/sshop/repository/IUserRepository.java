package com.ecommerce.sshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.sshop.model.User;

public interface IUserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
