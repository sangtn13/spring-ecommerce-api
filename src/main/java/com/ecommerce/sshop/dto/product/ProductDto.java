package com.ecommerce.sshop.dto.product;

import java.math.BigDecimal;
import java.util.List;

import com.ecommerce.sshop.dto.brand.BrandSummaryDto;
import com.ecommerce.sshop.dto.category.CategorySummaryDto;
import com.ecommerce.sshop.dto.image.ImageDto;

import lombok.Data;

@Data
public class ProductDto {
    private String id;
    private String name;
    private BrandSummaryDto brand;
    private BigDecimal price;
    private int inventory;
    private String description;
    private CategorySummaryDto category;
    private List<ImageDto> images;
}
