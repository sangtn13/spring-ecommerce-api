package com.ecommerce.sshop.model.product;

import java.math.BigDecimal;
import java.util.List;

import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.model.image.Image;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "char(36)")
    private String id;
    @Column(length = 150, nullable = false)
    private String name;
    @Column(length = 120)
    private String brand;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private int inventory;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    public Product(String name, String brand, BigDecimal price, int inventory, String description, Category category) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.inventory = inventory;
        this.description = description;
        this.category = category;
    }
}
