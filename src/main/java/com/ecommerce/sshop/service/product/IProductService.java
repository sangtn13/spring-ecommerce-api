package com.ecommerce.sshop.service.product;

import com.ecommerce.sshop.dto.ProductDto;
import com.ecommerce.sshop.model.Product;
import com.ecommerce.sshop.request.AddProductRequest;
import com.ecommerce.sshop.request.UpdateProductRequest;

import java.util.List;

public interface IProductService {

    Product addProduct(AddProductRequest request);


    Product getProductById(Long id);


    void deleteProduct(Long id);

    Product updateProduct(UpdateProductRequest product, Long productId);

    List<Product> getAllProducts();

    List<Product> getProductsByCategory(String category);

    List<Product> getProductsByBrand(String brand);

    List<Product> getProductsByCategoryAndBrand(String category, String brand);

    List<Product> getProductsByName(String name);

    List<Product> getProductsByBrandAndName(String brand, String name);

    Long countProductsByBrandAndName(String brand, String name);

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);
}
