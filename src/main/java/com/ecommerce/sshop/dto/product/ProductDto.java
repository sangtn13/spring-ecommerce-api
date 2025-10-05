package com.ecommerce.sshop.dto.product;

import java.math.BigDecimal;
import java.util.List;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.model.category.Category;

import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private int inventory;
    private String description;
    private Category category;
    private List<ImageDto> images;
}
