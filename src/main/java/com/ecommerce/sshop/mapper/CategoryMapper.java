package com.ecommerce.sshop.mapper;

import org.mapstruct.Mapper;

import com.ecommerce.sshop.dto.category.CategoryDto;
import com.ecommerce.sshop.model.category.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category category);
}
