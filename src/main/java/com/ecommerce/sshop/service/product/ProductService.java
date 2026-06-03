package com.ecommerce.sshop.service.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.dto.product.ProductDto;
import com.ecommerce.sshop.exception.brand.BrandNotFoundException;
import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.exception.product.InvalidProductRequestException;
import com.ecommerce.sshop.exception.product.ProductNotFoundException;
import com.ecommerce.sshop.exception.category.CategoryNotFoundException;
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
import com.ecommerce.sshop.util.ProductSearchUtil;
import com.ecommerce.sshop.util.StringUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private static final int MIN_FULLTEXT_TERM_LENGTH = 3;

    private final IProductRepository productRepository;
    private final IBrandRepository brandRepository;
    private final ICategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ImageMapper imageMapper;
    private final IImageRepository imageRepository;

    @Override
    public Product addProduct(AddProductRequest request) {
        validateRequiredCreateReferences(request);
        Brand brand = resolveBrandForCreate(request);
        Category category = resolveCategoryForCreate(request);
        String brandName = brand.getName();
        if (isProductExist(request.getName(), brandName)) {
            throw new AlreadyExistsException("Product with name " + request.getName() + " and brand "
                    + brandName + " already exists.");
        }

        return productRepository.save(createProduct(request, brand, category));
    }

    private void validateRequiredCreateReferences(AddProductRequest request) {
        if (!StringUtils.hasText(request.getBrandId())) {
            throw new InvalidProductRequestException("brandId is required");
        }
        if (!StringUtils.hasText(request.getCategoryId())) {
            throw new InvalidProductRequestException("categoryId is required");
        }
    }

    private Product createProduct(AddProductRequest request, Brand brand, Category category) {
        return new Product(request.getName(), brand, request.getPrice(), request.getInventory(),
                request.getDescription(), category);
    }

    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found!!"));
    }

    @Override
    public void deleteProduct(String id) {
        productRepository.findById(id).ifPresentOrElse(productRepository::delete, () -> {
            throw new ProductNotFoundException("Product not found!!");
        });
    }

    @Override
    public Product updateProduct(UpdateProductRequest request, String productId) {
        Product existingProduct = getProductById(productId);
        Brand targetBrand = resolveBrandForUpdate(request, existingProduct);
        String targetName = StringUtils.hasText(request.getName()) ? request.getName() : existingProduct.getName();

        boolean isChangingProductIdentity = !existingProduct.getName().equals(targetName)
                || !existingProduct.getBrand().getName().equals(targetBrand.getName());

        if (isChangingProductIdentity && isProductExist(targetName, targetBrand.getName())) {
            throw new AlreadyExistsException(
                    "Product with name " + targetName + " and brand " + targetBrand.getName() + " already exists.");
        }

        Product updatedProduct = updateExistingProduct(existingProduct, request, targetBrand);
        return productRepository.save(updatedProduct);
    }

    private Product updateExistingProduct(Product existingProduct, UpdateProductRequest request, Brand brand) {
        existingProduct.setName(request.getName());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setBrand(brand);

        Category category = resolveCategoryForUpdate(request);
        if (category != null) {
            existingProduct.setCategory(category);
        }
        return existingProduct;
    }

    private Category resolveCategoryForCreate(AddProductRequest request) {
        if (!StringUtils.hasText(request.getCategoryId())) {
            throw new InvalidProductRequestException("categoryId is required");
        }
        return categoryRepository.findById(request.getCategoryId().trim())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found!!"));
    }

    private Category resolveCategoryForUpdate(UpdateProductRequest request) {
        if (!StringUtils.hasText(request.getCategoryId())) {
            return null;
        }

        return categoryRepository.findById(request.getCategoryId().trim())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found!!"));
    }

    private Brand resolveBrandForCreate(AddProductRequest request) {
        if (!StringUtils.hasText(request.getBrandId())) {
            throw new InvalidProductRequestException("brandId is required");
        }
        return brandRepository.findById(request.getBrandId().trim())
                .orElseThrow(() -> new BrandNotFoundException("Brand not found!!"));
    }

    private Brand resolveBrandForUpdate(UpdateProductRequest request, Product existingProduct) {
        if (!StringUtils.hasText(request.getBrandId())) {
            return existingProduct.getBrand();
        }
        return brandRepository.findById(request.getBrandId().trim())
                .orElseThrow(() -> new BrandNotFoundException("Brand not found!!"));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrandName(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrandName(category, brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByNameStartingWith(name.trim());
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandNameAndNameStartingWith(brand, name.trim());
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        ProductDto productDto = productMapper.toDto(product);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream().map(imageMapper::toDto).toList();
        productDto.setImages(imageDtos);
        return productDto;
    }

    private boolean isProductExist(String name, String brand) {
        return StringUtils.hasText(name) && StringUtils.hasText(brand)
                && productRepository.existsByNameAndBrandName(name, brand);
    }

    @Override
    public Page<ProductDto> getAllProductsWithPaging(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public Page<ProductDto> getProductsByCategoryWithPaging(String category, Pageable pageable) {
        return productRepository.findByCategoryName(category, pageable).map(this::convertToDto);
    }

    @Override
    public Page<ProductDto> getProductsByBrandWithPaging(String brand, Pageable pageable) {
        return productRepository.findByBrandName(brand, pageable).map(this::convertToDto);
    }

    @Override
    public Page<ProductDto> searchProductsWithPaging(String brandId, String categoryId,
            String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        ProductSearchUtil.NameSearchFilter nameSearchFilter = ProductSearchUtil.buildNameSearchFilter(
                name,
                MIN_FULLTEXT_TERM_LENGTH);
        return productRepository.searchProductsByFilters(
                StringUtil.trimToNull(brandId),
                StringUtil.trimToNull(categoryId),
                nameSearchFilter.fullTextQuery(),
                nameSearchFilter.normalizedPhrase(),
                minPrice,
                maxPrice,
                pageable).map(this::convertToDto);
    }
}
