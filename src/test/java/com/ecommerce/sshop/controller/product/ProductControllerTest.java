package com.ecommerce.sshop.controller.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
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

    @Mock
    private IProductService productService;
    @InjectMocks
    private ProductController productController;

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

        ResponseEntity<ApiResponse> response = productController.getAllProducts(1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Products retrieved successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Get product by ID successfully")
    void getProductById_Success() {
        when(productService.getProductById(productId)).thenReturn(sampleProduct);
        when(productService.convertToDto(sampleProduct)).thenReturn(sampleProductDto);

        ResponseEntity<ApiResponse> response = productController.getProductById(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleProductDto, response.getBody().getData());
    }

    @Test
    @DisplayName("Add new product successfully (Admin)")
    void addProduct_Success() {
        AddProductRequest request = new AddProductRequest();
        when(productService.addProduct(request)).thenReturn(sampleProduct);
        when(productService.convertToDto(sampleProduct)).thenReturn(sampleProductDto);

        ResponseEntity<ApiResponse> response = productController.addProduct(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product added successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Update product successfully (Admin)")
    void updateProduct_Success() {
        UpdateProductRequest request = new UpdateProductRequest();
        when(productService.updateProduct(request, productId)).thenReturn(sampleProduct);
        when(productService.convertToDto(sampleProduct)).thenReturn(sampleProductDto);

        ResponseEntity<ApiResponse> response = productController.updateProduct(request, productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product updated successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Delete product successfully (Admin)")
    void deleteProduct_Success() {
        doNothing().when(productService).deleteProduct(productId);

        ResponseEntity<ApiResponse> response = productController.deleteProduct(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productId, response.getBody().getData());
    }

    @Test
    @DisplayName("Get product by name successfully")
    void getProductByName_Success() {
        when(productService.getProductsByName("iPhone 15")).thenReturn(List.of(sampleProduct));
        when(productService.getConvertedProducts(anyList())).thenReturn(List.of(sampleProductDto));

        ResponseEntity<ApiResponse> response = productController.getProductByName("iPhone 15");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Get product by name fails when name is invalid - Throws ProductNotFoundException")
    void getProductByName_NotFound() {
        // Gọi đúng hàm getProductsByName thay vì getProductsInverseName sai lệch trước
        // đó
        when(productService.getProductsByName("Unknown")).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse> response = productController.getProductByName("Unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by brand and name successfully")
    void getProductByBrandAndName_Success() {
        when(productService.getProductsByBrandAndName("Apple", "iPhone 15")).thenReturn(List.of(sampleProduct));
        when(productService.getConvertedProducts(anyList())).thenReturn(List.of(sampleProductDto));

        ResponseEntity<ApiResponse> response = productController.getProductByBrandAndName("Apple", "iPhone 15");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by brand and name fails when name is invalid - Throws ProductNotFoundException")
    void getProductByBrandAndName_NotFound() {
        when(productService.getProductsByBrandAndName("Unknown", "Unknown")).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse> response = productController.getProductByBrandAndName("Unknown", "Unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by category and brand successfully")
    void getProductByCategoryAndBrand_Success() {
        when(productService.getProductsByCategoryAndBrand("Electronics", "Apple")).thenReturn(List.of(sampleProduct));
        when(productService.getConvertedProducts(anyList())).thenReturn(List.of(sampleProductDto));

        ResponseEntity<ApiResponse> response = productController.getProductByCategoryAndBrand("Electronics", "Apple");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by category and brand fails when brand is invalid - Throws ProductNotFoundException")
    void getProductByCategoryAndBrand_NotFound() {
        when(productService.getProductsByCategoryAndBrand("Unknown", "Unknown")).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse> response = productController.getProductByCategoryAndBrand("Unknown", "Unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by brand with paging successfully")
    void findProductByBrandPaged_Success() {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleProductDto));
        when(productService.getProductsByBrandWithPaging(eq("Apple"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse> response = productController.findProductByBrandPaged("Apple", 1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by brand with paging fails when brand is invalid")
    void findProductByBrandPaged_NotFound() {
        when(productService.getProductsByBrandWithPaging(eq("Unknown"), any(Pageable.class))).thenReturn(Page.empty());

        ResponseEntity<ApiResponse> response = productController.findProductByBrandPaged("Unknown", 1, 5, "id", "asc");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by category with paging successfully")
    void getProductByCategoryPaged_Success() {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleProductDto));
        when(productService.getProductsByCategoryWithPaging(eq("Electronics"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse> response = productController.getProductByCategoryPaged("Electronics", 1, 5, "id",
                "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by category with paging fails when category is invalid")
    void getProductByCategoryPaged_NotFound() {
        when(productService.getProductsByCategoryWithPaging(eq("Unknown"), any(Pageable.class)))
                .thenReturn(Page.empty());

        ResponseEntity<ApiResponse> response = productController.getProductByCategoryPaged("Unknown", 1, 5, "id",
                "asc");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by keyword with paging successfully")
    void searchProductsPaged_Success() {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleProductDto));
        when(productService.searchProductsWithPaging(eq("iPhone"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse> response = productController.searchProductsPaged("iPhone", 1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Get product by keyword with paging fails when keyword is invalid")
    void searchProductsPaged_NotFound() {
        when(productService.searchProductsWithPaging(eq("Unknown"), any(Pageable.class))).thenReturn(Page.empty());

        ResponseEntity<ApiResponse> response = productController.searchProductsPaged("Unknown", 1, 5, "id", "asc");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Count products by brand and name successfully (Admin)")
    void getProductCountByBrandAndName_Success() {
        when(productService.countProductsByBrandAndName("Apple", "iPhone 15")).thenReturn(10L);

        ResponseEntity<ApiResponse> response = productController.getProductCountByBrandAndName("Apple", "iPhone 15");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getData());
    }
}