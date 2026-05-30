package com.ecommerce.sshop.service.image;

import java.util.List;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.model.image.Image;

import org.springframework.web.multipart.MultipartFile;

public interface IImageService {
    Image getImageById(String id);

    void deleteImageById(String id);

    List<ImageDto> saveImages(List<MultipartFile> file, String productId);

    void updateImage(MultipartFile file, String imageId);
}
