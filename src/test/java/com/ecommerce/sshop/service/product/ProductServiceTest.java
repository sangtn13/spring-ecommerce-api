package com.ecommerce.sshop.service.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.dto.product.ProductDto;
import com.ecommerce.sshop.exception.category.CategoryNotFoundException;
import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.exception.product.ProductNotFoundException;
import com.ecommerce.sshop.mapper.ImageMapper;
import com.ecommerce.sshop.mapper.ProductMapper;
import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.repository.category.ICategoryRepository;
import com.ecommerce.sshop.repository.image.IImageRepository;
import com.ecommerce.sshop.repository.product.IProductRepository;
import com.ecommerce.sshop.request.products.AddProductRequest;
import com.ecommerce.sshop.request.products.UpdateProductRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private IProductRepository productRepository;
    @Mock
    private ICategoryRepository categoryRepository;
    @Mock
    private IImageRepository imageRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ImageMapper imageMapper;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private Category sampleCategory;
    private final String productId = "prod-uuid-123";
    private final String categoryId = "cat-uuid-123";

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Electronics");
        sampleCategory.setId(categoryId);

        sampleProduct = new Product("iPhone 15", "Apple", new BigDecimal("999.00"), 50, "Smartphone", sampleCategory);
        sampleProduct.setId(productId);
    }

    @Test
    @DisplayName("Add new Product successfully with existing Category ID")
    void addProduct_WithCategoryId_Success() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrand("Apple");
        request.setCategoryId(categoryId);

        when(productRepository.existsByNameAndBrand(request.getName(), request.getBrand())).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product result = productService.addProduct(request);

        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Add new Product failed - Product already exists")
    void addProduct_AlreadyExists_ThrowsException() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrand("Apple");

        when(productRepository.existsByNameAndBrand(request.getName(), request.getBrand())).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> productService.addProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add new Product with new Category name successfully")
    void addProduct_WithNewCategoryName_Success() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrand("Apple");
        request.setCategoryName("New Tech");

        when(productRepository.existsByNameAndBrand(request.getName(), request.getBrand())).thenReturn(false);
        when(categoryRepository.findByName("New Tech")).thenReturn(null);
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category("New Tech"));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product result = productService.addProduct(request);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Add new Product failed - Missing Category name and Category ID, throws CategoryNotFoundException")
    void addProduct_MissingCategoryName_ThrowsException() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrand("Apple");

        when(productRepository.existsByNameAndBrand(request.getName(), request.getBrand())).thenReturn(false);

        assertThrows(CategoryNotFoundException.class, () -> productService.addProduct(request));
    }

    @Test
    @DisplayName("Get Product by ID successfully")
    void getProductById_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        Product result = productService.getProductById(productId);
        assertNotNull(result);
        assertEquals(productId, result.getId());
    }

    @Test
    @DisplayName("Get Product by ID failed - Not found")
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    @DisplayName("Update Product successfully with existing Category ID")
    void updateProduct_Success() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("iPhone 15 Pro");
        request.setBrand("Apple");
        request.setCategoryId(categoryId);

        when(productRepository.existsByNameAndBrand(request.getName(), request.getBrand())).thenReturn(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProduct(request, productId);

        assertNotNull(updated);
        assertEquals("iPhone 15 Pro", updated.getName());
    }

    @Test
    @DisplayName("Delete Product successfully when record is found")
    void deleteProduct_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).delete(sampleProduct);

        assertDoesNotThrow(() -> productService.deleteProduct(productId));
        verify(productRepository).delete(sampleProduct);
    }

    @Test
    @DisplayName("Search and paginate products successfully")
    void searchProductsWithPaging_Success() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.searchProducts("iPhone", pageable)).thenReturn(page);
        when(productMapper.toDto(sampleProduct)).thenReturn(new ProductDto());
        when(imageRepository.findByProductId(productId)).thenReturn(new ArrayList<>());

        Page<ProductDto> result = productService.searchProductsWithPaging("iPhone", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}