package com.ecommerce.sshop.service.product;

import java.util.List;

import com.ecommerce.sshop.dto.product.ProductDto;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.request.products.AddProductRequest;
import com.ecommerce.sshop.request.products.UpdateProductRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {

    Product addProduct(AddProductRequest request);

    Product getProductById(String id);

    void deleteProduct(String id);

    Product updateProduct(UpdateProductRequest product, String productId);

    List<Product> getAllProducts();

    List<Product> getProductsByCategory(String category);

    List<Product> getProductsByBrand(String brand);

    List<Product> getProductsByCategoryAndBrand(String category, String brand);

    List<Product> getProductsByName(String name);

    List<Product> getProductsByBrandAndName(String brand, String name);

    Long countProductsByBrandAndName(String brand, String name);

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);

    // Paging methods
    Page<ProductDto> getAllProductsWithPaging(Pageable pageable);
    
    Page<ProductDto> getProductsByCategoryWithPaging(String category, Pageable pageable);
    
    Page<ProductDto> getProductsByBrandWithPaging(String brand, Pageable pageable);
    
    Page<ProductDto> searchProductsWithPaging(String keyword, Pageable pageable);
}
