package com.ecommerce.sshop.model.carts;

import java.math.BigDecimal;

import com.ecommerce.sshop.model.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "char(36)")
    private String id;
    @Column(nullable = false)
    private int quantity;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    public void setTotalPrice() {
        if (this.unitPrice != null) {
            this.totalPrice = this.unitPrice.multiply(new BigDecimal(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

}
