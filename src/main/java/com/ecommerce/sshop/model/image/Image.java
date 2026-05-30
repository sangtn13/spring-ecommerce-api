package com.ecommerce.sshop.model.image;

import java.sql.Blob;

import com.ecommerce.sshop.model.product.Product;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "char(36)")
    private String id;
    @Column(length = 200)
    private String fileName;
    @Column(length = 100)
    private String fileType;

    @Lob
    private Blob image;
    @Column(length = 500)
    private String downloadUrl;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
