package com.ecommerce.sshop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.sshop.dto.brand.BrandSummaryDto;
import com.ecommerce.sshop.dto.category.CategorySummaryDto;
import com.ecommerce.sshop.dto.product.ProductDto;
import com.ecommerce.sshop.model.brand.Brand;
import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.model.product.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "images", ignore = true)
    ProductDto toDto(Product product);

    BrandSummaryDto toSummaryDto(Brand brand);

    CategorySummaryDto toSummaryDto(Category category);
}
