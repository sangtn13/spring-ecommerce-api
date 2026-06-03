package com.ecommerce.sshop.model.brand;

import java.util.List;

import com.ecommerce.sshop.model.base.BaseEntity;
import com.ecommerce.sshop.model.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Brand extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "char(36)")
    private String id;

    @Column(length = 120, nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "brand")
    @JsonIgnore
    private List<Product> products;

    public Brand(String name) {
        this.name = name;
    }
}
