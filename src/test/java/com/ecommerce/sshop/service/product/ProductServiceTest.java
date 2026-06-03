package com.ecommerce.sshop.service.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.dto.product.ProductDto;
import com.ecommerce.sshop.exception.brand.BrandNotFoundException;
import com.ecommerce.sshop.exception.category.CategoryNotFoundException;
import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.exception.product.InvalidProductRequestException;
import com.ecommerce.sshop.exception.product.ProductNotFoundException;
import com.ecommerce.sshop.mapper.ImageMapper;
import com.ecommerce.sshop.mapper.ProductMapper;
import com.ecommerce.sshop.model.brand.Brand;
import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.model.image.Image;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.repository.brand.IBrandRepository;
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

    @Mock private IProductRepository productRepository;
    @Mock private IBrandRepository brandRepository;
    @Mock private ICategoryRepository categoryRepository;
    @Mock private IImageRepository imageRepository;
    @Mock private ProductMapper productMapper;
    @Mock private ImageMapper imageMapper;

    @InjectMocks private ProductService productService;

    private Product sampleProduct;
    private Category sampleCategory;
    private Brand sampleBrand;
    private final String productId = "prod-uuid-123";
    private final String categoryId = "cat-uuid-123";
    private final String brandId = "brand-uuid-123";

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Electronics");
        sampleCategory.setId(categoryId);

        sampleBrand = new Brand("Apple");
        sampleBrand.setId(brandId);

        sampleProduct = new Product("iPhone 15", sampleBrand, new BigDecimal("999.00"), 50, "Smartphone",
                sampleCategory);
        sampleProduct.setId(productId);
    }

    @Test
    @DisplayName("Add new product successfully with existing category id and brand id")
    void addProduct_WithCategoryIdAndBrandId_Success() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrandId(brandId);
        request.setPrice(new BigDecimal("999.00"));
        request.setInventory(50);
        request.setCategoryId(categoryId);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));
        when(productRepository.existsByNameAndBrandName(request.getName(), sampleBrand.getName())).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product result = productService.addProduct(request);

        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Add product fails when name and brand already exist")
    void addProduct_AlreadyExists_ThrowsException() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrandId(brandId);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        request.setCategoryId(categoryId);
        when(productRepository.existsByNameAndBrandName("iPhone 15", "Apple")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> productService.addProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add product fails when brand id is missing")
    void addProduct_MissingBrandId_ThrowsException() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setPrice(new BigDecimal("999.00"));
        request.setInventory(50);
        request.setCategoryId(categoryId);

        assertThrows(InvalidProductRequestException.class, () -> productService.addProduct(request));
    }

    @Test
    void addProduct_MissingCategoryId_ThrowsException() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrandId(brandId);

        assertThrows(InvalidProductRequestException.class, () -> productService.addProduct(request));
    }

    @Test
    void addProduct_BrandIdNotFound_ThrowsException() {
        AddProductRequest request = new AddProductRequest();
        request.setName("iPhone 15");
        request.setBrandId("missing-brand");
        request.setCategoryId(categoryId);

        when(brandRepository.findById("missing-brand")).thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class, () -> productService.addProduct(request));
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        Product result = productService.getProductById(productId);
        assertNotNull(result);
        assertEquals(productId, result.getId());
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void updateProduct_Success() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("iPhone 15 Pro");
        request.setBrandId(brandId);
        request.setCategoryId(categoryId);
        request.setPrice(new BigDecimal("1099.00"));
        request.setInventory(40);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));
        when(productRepository.existsByNameAndBrandName("iPhone 15 Pro", "Apple")).thenReturn(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProduct(request, productId);

        assertNotNull(updated);
        assertEquals("iPhone 15 Pro", updated.getName());
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).delete(sampleProduct);

        assertDoesNotThrow(() -> productService.deleteProduct(productId));
        verify(productRepository).delete(sampleProduct);
    }

    @Test
    void updateProduct_AlreadyExists_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Another Product");
        request.setBrandId(brandId);

        Product current = new Product();
        current.setName("Old Name");
        current.setBrand(sampleBrand);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));
        when(productRepository.existsByNameAndBrandName("Another Product", "Apple")).thenReturn(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(current));

        assertThrows(AlreadyExistsException.class, () -> productService.updateProduct(request, productId));
    }

    @Test
    void addProduct_CategoryIdNotFound_ThrowsException() {
        AddProductRequest request = new AddProductRequest();
        request.setName("n");
        request.setBrandId(brandId);
        request.setCategoryId("missing-id");

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));
        when(categoryRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> productService.addProduct(request));
    }

    @Test
    void getAllAndFilterMethods_DelegateToRepository() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));
        when(productRepository.findByCategoryName("Electronics")).thenReturn(List.of(sampleProduct));
        when(productRepository.findByBrandName("Apple")).thenReturn(List.of(sampleProduct));
        when(productRepository.findByCategoryNameAndBrandName("Electronics", "Apple")).thenReturn(List.of(sampleProduct));
        when(productRepository.findByNameStartingWith("iPhone")).thenReturn(List.of(sampleProduct));
        when(productRepository.findByBrandNameAndNameStartingWith("Apple", "iPhone")).thenReturn(List.of(sampleProduct));
        assertEquals(1, productService.getAllProducts().size());
        assertEquals(1, productService.getProductsByCategory("Electronics").size());
        assertEquals(1, productService.getProductsByBrand("Apple").size());
        assertEquals(1, productService.getProductsByCategoryAndBrand("Electronics", "Apple").size());
        assertEquals(1, productService.getProductsByName("iPhone").size());
        assertEquals(1, productService.getProductsByBrandAndName("Apple", "iPhone").size());
    }

    @Test
    void convertToDto_SetsImages() {
        ProductDto dto = new ProductDto();
        Image image = new Image();
        ImageDto imageDto = new ImageDto();

        when(productMapper.toDto(sampleProduct)).thenReturn(dto);
        when(imageRepository.findByProductId(productId)).thenReturn(List.of(image));
        when(imageMapper.toDto(image)).thenReturn(imageDto);

        ProductDto result = productService.convertToDto(sampleProduct);

        assertNotNull(result.getImages());
        assertEquals(1, result.getImages().size());
    }

    @Test
    void pagingMethods_Success() {
        ProductDto dto = new ProductDto();
        when(productMapper.toDto(sampleProduct)).thenReturn(dto);
        when(imageRepository.findByProductId(productId)).thenReturn(List.of());

        Pageable pageable = PageRequest.of(0, 5);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(pageable)).thenReturn(page);
        when(productRepository.findByCategoryName("Electronics", pageable)).thenReturn(page);
        when(productRepository.findByBrandName("Apple", pageable)).thenReturn(page);

        assertEquals(1, productService.getAllProductsWithPaging(pageable).getContent().size());
        assertEquals(1, productService.getProductsByCategoryWithPaging("Electronics", pageable).getContent().size());
        assertEquals(1, productService.getProductsByBrandWithPaging("Apple", pageable).getContent().size());
    }

    @Test
    @DisplayName("Search products with optional filters, full-text name, and price range")
    void searchProductsWithPaging_ByFilters_Success() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.searchProductsByFilters(eq(brandId), eq(null),
                eq("+pro* +max*"), eq(null), eq(new BigDecimal("500.00")), eq(new BigDecimal("1500.00")),
                eq(pageable)))
                .thenReturn(page);
        when(productMapper.toDto(sampleProduct)).thenReturn(new ProductDto());
        when(imageRepository.findByProductId(productId)).thenReturn(new ArrayList<>());

        Page<ProductDto> result = productService.searchProductsWithPaging(
                brandId,
                null,
                "Pro Max",
                new BigDecimal("500.00"),
                new BigDecimal("1500.00"),
                pageable);

        assertNotNull(result);
        verify(productRepository).searchProductsByFilters(eq(brandId), eq(null),
                eq("+pro* +max*"), eq(null), eq(new BigDecimal("500.00")), eq(new BigDecimal("1500.00")),
                eq(pageable));
    }

    @Test
    @DisplayName("Split punctuation and use exact phrase fallback when name contains short terms")
    void searchProductsWithPaging_NameFullTextNormalization() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.searchProductsByFilters(eq(null), eq(null),
                eq("+vertex*"), eq("vertex 14"), eq(null), eq(null), eq(pageable)))
                .thenReturn(page);
        when(productMapper.toDto(sampleProduct)).thenReturn(new ProductDto());
        when(imageRepository.findByProductId(productId)).thenReturn(new ArrayList<>());

        Page<ProductDto> result = productService.searchProductsWithPaging(
                null,
                null,
                "Vertex 14",
                null,
                null,
                pageable);

        assertNotNull(result);
        verify(productRepository).searchProductsByFilters(eq(null), eq(null),
                eq("+vertex*"), eq("vertex 14"), eq(null), eq(null), eq(pageable));
    }

    @Test
    @DisplayName("Use phrase-only fallback when name contains only short tokens")
    void searchProductsWithPaging_ShortNamePhraseOnly() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.searchProductsByFilters(eq(null), eq(null),
                eq(null), eq("14"), eq(null), eq(null), eq(pageable)))
                .thenReturn(page);
        when(productMapper.toDto(sampleProduct)).thenReturn(new ProductDto());
        when(imageRepository.findByProductId(productId)).thenReturn(new ArrayList<>());

        Page<ProductDto> result = productService.searchProductsWithPaging(
                null,
                null,
                "14",
                null,
                null,
                pageable);

        assertNotNull(result);
        verify(productRepository).searchProductsByFilters(eq(null), eq(null),
                eq(null), eq("14"), eq(null), eq(null), eq(pageable));
    }
}
