package com.ecommerce.sshop.service.image;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import java.sql.SQLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.exception.image.ImageNotFoundException;
import com.ecommerce.sshop.model.image.Image;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.repository.image.IImageRepository;
import com.ecommerce.sshop.service.product.IProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {
    private final IImageRepository imageRepository;
    private final IProductService productService;

    @Override
    public Image getImageById(String id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: !!" + id));
    }

    @Override
    public void deleteImageById(String id) {
        imageRepository.findById(id)
                .ifPresentOrElse(imageRepository::delete,
                        () -> new ImageNotFoundException("Image not found with id: !!" + id));
    }

    @Override
    public List<ImageDto> saveImages(List<MultipartFile> file, String productId) {
        Product product = productService.getProductById(productId);
        List<ImageDto> savedImageDto = new ArrayList<>();
        for (MultipartFile fileItem : file) {
            try {
                Image image = new Image();
                image.setFileName(fileItem.getOriginalFilename());
                image.setFileType(fileItem.getContentType());
                image.setImage(new SerialBlob(fileItem.getBytes()));
                image.setProduct(product);

                String buildDownloadUrl = "/api/v1/images/image/download/";
                String downloadUrl = buildDownloadUrl + image.getId();
                image.setDownloadUrl(downloadUrl);

                Image savedImage = imageRepository.save(image);
                savedImage.setDownloadUrl(buildDownloadUrl + savedImage.getId());

                imageRepository.save(savedImage);

                ImageDto imageDto = new ImageDto();
                imageDto.setId(savedImage.getId());
                imageDto.setFileName(savedImage.getFileName());
                imageDto.setDownloadUrl(savedImage.getDownloadUrl());
                savedImageDto.add(imageDto);

            } catch (SerialException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return savedImageDto;
    }

    @Override
    public void updateImage(MultipartFile file, String imageId) {
        Image image = getImageById(imageId);
        try {
            image.setFileName(file.getOriginalFilename());
            image.setImage(new SerialBlob(file.getBytes()));
            imageRepository.save(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SerialException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
