package com.ecommerce.sshop.service.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.exception.image.ImageNotFoundException;
import com.ecommerce.sshop.model.image.Image;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.repository.image.IImageRepository;
import com.ecommerce.sshop.service.product.IProductService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock private IImageRepository imageRepository;
    @Mock private IProductService productService;

    @InjectMocks private ImageService imageService;

    private final String imageId = "img-123";

    @Test
    @DisplayName("Get image by ID successfully")
    void getImageById_Success() {
        Image mockImage = new Image();
        mockImage.setId(imageId);
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(mockImage));

        Image result = imageService.getImageById(imageId);

        assertNotNull(result);
        assertEquals(imageId, result.getId());
    }

    @Test
    @DisplayName("Get image by ID fails when ID is invalid - Throws ImageNotFoundException")
    void getImageById_NotFound_ThrowsException() {
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class, () -> imageService.getImageById(imageId));
    }

    @Test
    @DisplayName("Save list of product images successfully")
    void saveImages_Success() {
        String productId = "prod-123";
        Product mockProduct = new Product();
        mockProduct.setId(productId);

        MockMultipartFile file = new MockMultipartFile(
                "files", "avatar.png", "image/png", "bytes-data".getBytes()
        );

        when(productService.getProductById(productId)).thenReturn(mockProduct);
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<ImageDto> result = imageService.saveImages(List.of(file), productId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("avatar.png", result.get(0).getFileName());
    }

    @Test
    @DisplayName("Update image binary data successfully")
    void updateImage_Success() {
        Image mockImage = new Image();
        mockImage.setId(imageId);
        MockMultipartFile file = new MockMultipartFile("file", "new.png", "image/png", "new-bytes".getBytes());

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(mockImage));
        when(imageRepository.save(any(Image.class))).thenReturn(mockImage);

        assertDoesNotThrow(() -> imageService.updateImage(file, imageId));
        verify(imageRepository).save(mockImage);
    }
}