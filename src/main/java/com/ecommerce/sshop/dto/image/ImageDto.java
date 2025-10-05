package com.ecommerce.sshop.dto.image;

import lombok.Data;

@Data
public class ImageDto {
    private Long id;
    private String fileName;
    private String downloadUrl;
}
