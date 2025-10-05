package com.ecommerce.sshop.repository.role;

import java.util.Optional;

import com.ecommerce.sshop.model.role.Role;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String roleName);

}
