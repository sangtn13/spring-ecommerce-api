package com.ecommerce.sshop.controller.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import com.ecommerce.sshop.dto.product.ProductDto;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.request.products.AddProductRequest;
import com.ecommerce.sshop.request.products.UpdateProductRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.product.IProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock private IProductService productService;
    @InjectMocks private ProductController productController;

    private Product sampleProduct;
    private ProductDto sampleProductDto;
    private final String productId = "prod-123";

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(productId);
        sampleProduct.setName("iPhone 15");

        sampleProductDto = new ProductDto();
        sampleProductDto.setId(productId);
        sampleProductDto.setName("iPhone 15");
    }

    @Test
    @DisplayName("Get all products with paging successfully")
    void getAllProducts_Success() {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleProductDto));
        when(productService.getAllProductsWithPaging(any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse> response = productController.getProducts(
                null, null, null, null, null, 1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Products retrieved successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Get products by combined filters successfully")
    void getProducts_ByFilters_Success() {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleProductDto));
        when(productService.searchProductsWithPaging(eq("brand-1"), eq("cat-1"),
                eq("iPhone"), eq(new BigDecimal("500")), eq(new BigDecimal("1500")), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<ApiResponse> response = productController.getProducts("brand-1", "cat-1",
                "iPhone", new BigDecimal("500"), new BigDecimal("1500"), 1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Products retrieved successfully", response.getBody().getMessage());
    }

    @Test
    void getProducts_InvalidPriceRange_ReturnsBadRequest() {
        ResponseEntity<ApiResponse> response = productController.getProducts(
                null, null, null, new BigDecimal("200"), new BigDecimal("100"), 1, 5, "id", "asc");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("minPrice must be less than or equal to maxPrice", response.getBody().getMessage());
    }

    @Test
    void getProducts_ByFilters_NotFound() {
        when(productService.searchProductsWithPaging(eq("brand-1"), eq(null), eq(null), eq(null),
                eq(null), any(Pageable.class))).thenReturn(Page.empty());

        ResponseEntity<ApiResponse> response = productController.getProducts(
                "brand-1", null, null, null, null, 1, 5, "id", "asc");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getProductById_Success() {
        when(productService.getProductById(productId)).thenReturn(sampleProduct);
        when(productService.convertToDto(sampleProduct)).thenReturn(sampleProductDto);

        ResponseEntity<ApiResponse> response = productController.getProductById(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleProductDto, response.getBody().getData());
    }

    @Test
    void addProduct_Success() {
        AddProductRequest request = new AddProductRequest();
        when(productService.addProduct(request)).thenReturn(sampleProduct);
        when(productService.convertToDto(sampleProduct)).thenReturn(sampleProductDto);

        ResponseEntity<ApiResponse> response = productController.addProduct(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product added successfully", response.getBody().getMessage());
    }

    @Test
    void updateProduct_Success() {
        UpdateProductRequest request = new UpdateProductRequest();
        when(productService.updateProduct(request, productId)).thenReturn(sampleProduct);
        when(productService.convertToDto(sampleProduct)).thenReturn(sampleProductDto);

        ResponseEntity<ApiResponse> response = productController.updateProduct(request, productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product updated successfully", response.getBody().getMessage());
    }

    @Test
    void deleteProduct_Success() {
        doNothing().when(productService).deleteProduct(productId);

        ResponseEntity<ApiResponse> response = productController.deleteProduct(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productId, response.getBody().getData());
    }

}
