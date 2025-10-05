package com.ecommerce.sshop.service.product;

import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.dto.image.ImageDto;
import com.ecommerce.sshop.dto.product.ProductDto;
import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.exception.product.ProductNotFoundException;
import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.model.image.Image;
import com.ecommerce.sshop.model.product.Product;
import com.ecommerce.sshop.repository.category.ICategoryRepository;
import com.ecommerce.sshop.repository.image.IImageRepository;
import com.ecommerce.sshop.repository.product.IProductRepository;
import com.ecommerce.sshop.request.products.AddProductRequest;
import com.ecommerce.sshop.request.products.UpdateProductRequest;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final IImageRepository imageRepository;

    @Override
    public Product addProduct(AddProductRequest request) {

        if (isProductExist(request.getName(), request.getBrand())) {
            throw new AlreadyExistsException("Product with name " + request.getName() + " and brand "
                    + request.getBrand() + " already exists.");
        }

        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
                .orElseGet(() -> {
                    Category newCategory = new Category(request.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        request.setCategory(category);
        return productRepository.save(createProduct(request, category));
    }

    private Product createProduct(AddProductRequest request, Category category) {
        return new Product(request.getName(), request.getBrand(), request.getPrice(), request.getInventory(),
                request.getDescription(), category);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found!!"));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresentOrElse(productRepository::delete, () -> {
            throw new ProductNotFoundException("Product not found!!");
        });
    }

    @Override
    public Product updateProduct(UpdateProductRequest product, Long productId) {

        if (isProductExist(product.getName(), product.getBrand())
                && !getProductById(productId).getName().equals(product.getName())) {
            throw new AlreadyExistsException(
                    "Product with name " + product.getName() + " and brand " + product.getBrand() + " already exists.");
        }

        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, product)).map(productRepository::save)
                .orElseThrow(() -> new ProductNotFoundException("Product not found!!"));
    }

    private Product updateExistingProduct(Product existingProduct, UpdateProductRequest request) {
        existingProduct.setName(request.getName());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());

        Category category = categoryRepository.findByName(request.getCategory().getName());
        existingProduct.setCategory(category);
        return existingProduct;
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
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream()
                .map(image -> modelMapper.map(image, ImageDto.class)).toList();
        productDto.setImages(imageDtos);
        return productDto;
    }

    private boolean isProductExist(String name, String brand) {
        return productRepository.existsByNameAndBrand(name, brand);
    }
}
