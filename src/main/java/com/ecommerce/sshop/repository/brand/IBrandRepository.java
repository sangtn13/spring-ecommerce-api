package com.ecommerce.sshop.repository.brand;

import com.ecommerce.sshop.model.brand.Brand;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IBrandRepository extends JpaRepository<Brand, String> {
    Brand findByName(String name);

    boolean existsByName(String name);
}
