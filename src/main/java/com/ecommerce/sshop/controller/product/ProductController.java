package com.ecommerce.sshop.controller.product;

import java.math.BigDecimal;

import com.ecommerce.sshop.dto.product.ProductDto;
import com.ecommerce.sshop.exception.product.ProductNotFoundException;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.request.products.AddProductRequest;
import com.ecommerce.sshop.request.products.UpdateProductRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.PagedResponse;
import com.ecommerce.sshop.service.product.IProductService;
import com.ecommerce.sshop.util.PageUtil;
import com.ecommerce.sshop.util.StringUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {
    private final IProductService productService;

    @GetMapping()
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) throws ProductNotFoundException {
        String normalizedBrandId = StringUtil.trimToNull(brandId);
        String normalizedCategoryId = StringUtil.trimToNull(categoryId);
        String normalizedName = StringUtil.trimToNull(name);

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("minPrice must be less than or equal to maxPrice", null));
        }

        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        if (normalizedBrandId == null && normalizedCategoryId == null
                && normalizedName == null && minPrice == null && maxPrice == null) {
            Page<ProductDto> productPage = productService.getAllProductsWithPaging(pageable);
            PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productPage);
            return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", pagedResponse));
        }

        Page<ProductDto> productPage = productService.searchProductsWithPaging(
                normalizedBrandId,
                normalizedCategoryId,
                normalizedName,
                minPrice,
                maxPrice,
                pageable);

        if (productPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Product not found!", HttpStatus.NOT_FOUND));
        }

        PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productPage);
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", pagedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        ProductDto convertedProduct = productService.convertToDto(product);
        return ResponseEntity.ok(new ApiResponse("Product retrieved successfully", convertedProduct));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @PostMapping()
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest product) {
        Product createdProduct = productService.addProduct(product);
        ProductDto convertedProduct = productService.convertToDto(createdProduct);
        return ResponseEntity.ok(new ApiResponse("Product added successfully", convertedProduct));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(@RequestBody UpdateProductRequest request,
            @PathVariable String productId) {
        Product updatedProduct = productService.updateProduct(request, productId);
        ProductDto convertedProduct = productService.convertToDto(updatedProduct);
        return ResponseEntity.ok(new ApiResponse("Product updated successfully", convertedProduct));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(new ApiResponse("Product deleted successfully", productId));
    }
}
