package com.ecommerce.sshop.repository.user;

import com.ecommerce.sshop.model.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    User findByEmail(String email);
}
