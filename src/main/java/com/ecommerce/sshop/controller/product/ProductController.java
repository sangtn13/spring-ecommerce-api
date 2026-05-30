package com.ecommerce.sshop.controller.product;

import java.util.List;

import com.ecommerce.sshop.dto.product.ProductDto;
import com.ecommerce.sshop.exception.product.ProductNotFoundException;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.request.products.AddProductRequest;
import com.ecommerce.sshop.request.products.UpdateProductRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.PagedResponse;
import com.ecommerce.sshop.service.product.IProductService;
import com.ecommerce.sshop.util.PageUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {
    private final IProductService productService;

    @GetMapping()
    public ResponseEntity<ApiResponse> getAllProducts(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) throws ProductNotFoundException {

        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);

        Page<ProductDto> productPage = productService.getAllProductsWithPaging(pageable);
        PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productPage);
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", pagedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        ProductDto convertedProduct = productService.convertToDto(product);
        return ResponseEntity.ok(new ApiResponse("Product retrieved successfully", convertedProduct));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping()
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest product) {
        Product theProduct = productService.addProduct(product);
        ProductDto convertedProduct = productService.convertToDto(theProduct);
        return ResponseEntity.ok(new ApiResponse("Product added successfully", convertedProduct));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(@RequestBody UpdateProductRequest request,
            @PathVariable String productId) {
        Product theProduct = productService.updateProduct(request, productId);
        ProductDto convertedProduct = productService.convertToDto(theProduct);
        return ResponseEntity.ok(new ApiResponse("Product updated successfully", convertedProduct));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(new ApiResponse("Product deleted successfully", productId));
    }

    @GetMapping("/by-brand-and-name")
    public ResponseEntity<ApiResponse> getProductByBrandAndName(@RequestParam String brand, @RequestParam String name) {
        List<Product> products = productService.getProductsByBrandAndName(brand, name);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Product not found!", HttpStatus.NOT_FOUND));
        }
        List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
        return ResponseEntity.ok(new ApiResponse("Success", convertedProducts));
    }

    @GetMapping("/by-category-and-brand")
    public ResponseEntity<ApiResponse> getProductByCategoryAndBrand(@RequestParam String category,
            @RequestParam String brand) {
        List<Product> products = productService.getProductsByCategoryAndBrand(category, brand);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Product not found!", HttpStatus.NOT_FOUND));
        }
        List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
        return ResponseEntity.ok(new ApiResponse("Success", convertedProducts));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse> getProductByName(@PathVariable String name) {
        List<Product> products = productService.getProductsByName(name);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Product not found!", HttpStatus.NOT_FOUND));
        }
        List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
        return ResponseEntity.ok(new ApiResponse("Success", convertedProducts));
    }

    @GetMapping("/by-brand")
    public ResponseEntity<ApiResponse> findProductByBrandPaged(
            @RequestParam String brand,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<ProductDto> productPage = productService.getProductsByBrandWithPaging(brand, pageable);

        if (productPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Product not found!", HttpStatus.NOT_FOUND));
        }

        PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productPage);
        return ResponseEntity.ok(new ApiResponse("Products by brand retrieved successfully", pagedResponse));
    }

    @GetMapping("/by-category/{category}")
    public ResponseEntity<ApiResponse> getProductByCategoryPaged(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<ProductDto> productPage = productService.getProductsByCategoryWithPaging(category, pageable);

        if (productPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Product not found!", HttpStatus.NOT_FOUND));
        }

        PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productPage);
        return ResponseEntity.ok(new ApiResponse("Products by category retrieved successfully", pagedResponse));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchProductsPaged(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<ProductDto> productPage = productService.searchProductsWithPaging(keyword, pageable);

        if (productPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("No products found matching the search criteria!", HttpStatus.NOT_FOUND));
        }

        PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productPage);
        return ResponseEntity.ok(new ApiResponse("Search results retrieved successfully", pagedResponse));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse> getProductCountByBrandAndName(@RequestParam String brand,
            @RequestParam String name) {
        long count = productService.countProductsByBrandAndName(brand, name);
        return ResponseEntity.ok(new ApiResponse("Product count retrieved successfully", count));
    }
}
