package com.ecommerce.sshop.mapper;

import org.mapstruct.Mapper;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.model.image.Image;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageDto toDto(Image image);
}
