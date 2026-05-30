package com.ecommerce.sshop.controller.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Blob;
import java.util.List;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.model.image.Image;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.image.IImageService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock private IImageService imageService;

    @InjectMocks private ImageController imageController;

    private final String imageId = "img-123";

    @Test
    @DisplayName("Upload images successfully (Admin)")
    void saveImages_Success() {
        MockMultipartFile file = new MockMultipartFile("files", "test.jpg", "image/jpeg", "data".getBytes());
        ImageDto mockDto = new ImageDto();
        when(imageService.saveImages(anyList(), eq("prod-123"))).thenReturn(List.of(mockDto));

        ResponseEntity<ApiResponse> response = imageController.saveImages(List.of(file), "prod-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Images uploaded successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Download image successfully (Admin)")
    void downloadImage_Success() throws Exception {
        Image mockImage = new Image();
        mockImage.setFileType("image/jpeg");
        mockImage.setFileName("test.jpg");
        
        Blob mockBlob = mock(Blob.class);
        when(mockBlob.getBytes(1, 0)).thenReturn(new byte[0]);
        when(mockBlob.length()).thenReturn(0L);
        mockImage.setImage(mockBlob);

        when(imageService.getImageById(imageId)).thenReturn(mockImage);

        ResponseEntity<Resource> response = imageController.downloadImage(imageId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Content-Disposition"));
    }

    @Test
    @DisplayName("Update image successfully (Admin)")
    void updateImage_Success() {
        Image mockImage = new Image();
        MockMultipartFile file = new MockMultipartFile("file", "update.jpg", "image/jpeg", "new-data".getBytes());
        
        when(imageService.getImageById(imageId)).thenReturn(mockImage);
        doNothing().when(imageService).updateImage(file, imageId);

        ResponseEntity<ApiResponse> response = imageController.updateImage(imageId, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Image updated successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Update image failed when record is not found (Admin)")
    void updateImage_NotFound() {
        MockMultipartFile file = new MockMultipartFile("file", "update.jpg", "image/jpeg", "new-data".getBytes());
        when(imageService.getImageById(imageId)).thenReturn(null);

        ResponseEntity<ApiResponse> response = imageController.updateImage(imageId, file);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Delete image successfully (Admin)")
    void deleteImage_Success() {
        Image mockImage = new Image();
        when(imageService.getImageById(imageId)).thenReturn(mockImage);
        doNothing().when(imageService).deleteImageById(imageId);

        ResponseEntity<ApiResponse> response = imageController.deleteImage(imageId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}