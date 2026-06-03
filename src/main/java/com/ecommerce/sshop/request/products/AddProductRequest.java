package com.ecommerce.sshop.request.products;

import java.math.BigDecimal;


import lombok.Data;

@Data
public class AddProductRequest {
    private String name;
    private String brandId;
    private BigDecimal price;
    private int inventory;
    private String description;
    private String categoryId;
}
