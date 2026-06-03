package com.ecommerce.sshop.service.category;

import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.exception.category.CategoryNotFoundException;
import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.repository.category.ICategoryRepository;
import com.ecommerce.sshop.request.categories.UpsertCategoryRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
    private final ICategoryRepository categoryRepository;

    @Override
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found!!"));
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category addCategory(UpsertCategoryRequest request) {
        String categoryName = request.getName();
        if (categoryRepository.existsByName(categoryName)) {
            throw new AlreadyExistsException(categoryName + " already exists");
        }
        return categoryRepository.save(new Category(categoryName));
    }

    @Override
    public Category updateCategory(UpsertCategoryRequest request, String id) {
        String categoryName = request.getName();
        if (categoryRepository.existsByName(categoryName)
                && !getCategoryById(id).getName().equals(categoryName)) {
            throw new AlreadyExistsException(categoryName + " already exists");
        }
        return Optional.ofNullable(getCategoryById(id)).map(oldCategory -> {
            oldCategory.setName(categoryName);
            return categoryRepository.save(oldCategory);
        }).orElseThrow(() -> new CategoryNotFoundException("Category not found!!"));
    }

    @Override
    public void deleteCategoryById(String id) {
        categoryRepository.findById(id).ifPresentOrElse(categoryRepository::delete, () -> {
            throw new CategoryNotFoundException("Category not found!!");
        });
    }

    @Override
    public Page<Category> getAllCategoriesWithPaging(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
}
