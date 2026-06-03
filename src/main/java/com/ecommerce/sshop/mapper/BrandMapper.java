package com.ecommerce.sshop.mapper;

import org.mapstruct.Mapper;

import com.ecommerce.sshop.dto.brand.BrandDto;
import com.ecommerce.sshop.model.brand.Brand;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandDto toDto(Brand brand);
}
