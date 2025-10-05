package com.ecommerce.sshop.repository.category;

import com.ecommerce.sshop.model.category.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);

    boolean existsByName(String name);
}
