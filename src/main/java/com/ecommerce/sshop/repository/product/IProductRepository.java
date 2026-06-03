package com.ecommerce.sshop.repository.product;

import java.util.List;

import com.ecommerce.sshop.model.product.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface IProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    List<Product> findByCategoryName(String category);

    List<Product> findByBrandName(String brand);

    List<Product> findByCategoryNameAndBrandName(String category, String brand);

    List<Product> findByNameStartingWith(String name);

    List<Product> findByBrandNameAndNameStartingWith(String brand, String name);

    boolean existsByNameAndBrandName(String name, String brand);

    Page<Product> findByCategoryName(String category, Pageable pageable);

    Page<Product> findByBrandName(String brand, Pageable pageable);

    @Query(value = """
            SELECT p.*
            FROM product p
            JOIN brand b ON p.brand_id = b.id
            JOIN category c ON p.category_id = c.id
            WHERE (?1 IS NULL OR b.id = ?1)
              AND (?2 IS NULL OR c.id = ?2)
              AND (?3 IS NULL OR MATCH(p.name) AGAINST (?3 IN BOOLEAN MODE))
              AND (?4 IS NULL OR LOCATE(?4, TRIM(REGEXP_REPLACE(LOWER(p.name), '[^[:alnum:]]+', ' '))) > 0)
              AND (?5 IS NULL OR p.price >= ?5)
              AND (?6 IS NULL OR p.price <= ?6)
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM product p
            JOIN brand b ON p.brand_id = b.id
            JOIN category c ON p.category_id = c.id
            WHERE (?1 IS NULL OR b.id = ?1)
              AND (?2 IS NULL OR c.id = ?2)
              AND (?3 IS NULL OR MATCH(p.name) AGAINST (?3 IN BOOLEAN MODE))
              AND (?4 IS NULL OR LOCATE(?4, TRIM(REGEXP_REPLACE(LOWER(p.name), '[^[:alnum:]]+', ' '))) > 0)
              AND (?5 IS NULL OR p.price >= ?5)
              AND (?6 IS NULL OR p.price <= ?6)
            """,
            nativeQuery = true)
    Page<Product> searchProductsByFilters(
            String brandId,
            String categoryId,
            String fullTextName,
            String normalizedNamePhrase,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            Pageable pageable);

}
