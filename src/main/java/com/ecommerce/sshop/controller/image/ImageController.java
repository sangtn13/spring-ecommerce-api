package com.ecommerce.sshop.controller.image;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.model.image.Image;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.image.IImageService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/images")
public class ImageController {
    private final IImageService imageService;

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> saveImages(@RequestPart("files") List<MultipartFile> file,
            @RequestParam String productId) {
        List<ImageDto> imageDtos = imageService.saveImages(file, productId);
        return ResponseEntity.ok(new ApiResponse("Images uploaded successfully", imageDtos));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @GetMapping("/download/{imageId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String imageId) throws SQLException {
        Image image = imageService.getImageById(imageId);
        if (image.getImage() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        byte[] imageBytes = image.getImage().getBytes(1, (int) image.getImage().length());
        ByteArrayResource resource = new ByteArrayResource(Objects.requireNonNull(imageBytes, "Image data is null"));
        String fileType = image.getFileType();
        String fileName = image.getFileName();
        if (fileType == null || fileName == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(fileType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @PutMapping(value = "/image/{imageId}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> updateImage(@PathVariable String imageId,
            @RequestPart("file") MultipartFile file) {
        Image image = imageService.getImageById(imageId);
        if (image != null) {
            ImageDto updatedImageDto = imageService.updateImage(file, imageId);
            return ResponseEntity.ok(new ApiResponse("Image updated successfully", updatedImageDto));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Update failed!", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @DeleteMapping("/image/{imageId}/delete")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable String imageId) {
        Image image = imageService.getImageById(imageId);
        if (image != null) {
            imageService.deleteImageById(imageId);
            return ResponseEntity.ok(new ApiResponse("Image deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Delete failed!", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
