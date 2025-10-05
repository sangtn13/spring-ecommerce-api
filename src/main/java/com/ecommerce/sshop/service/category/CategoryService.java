package com.ecommerce.sshop.service.category;

import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.exception.category.CategoryNotFoundException;
import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.repository.category.ICategoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
    private final ICategoryRepository categoryRepository;

    @Override
    public Category getCategoryById(Long id) {
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
    public Category addCategory(Category category) {
        return Optional.of(category).filter(c -> !categoryRepository.existsByName(c.getName()))
                .map(categoryRepository::save)
                .orElseThrow(() -> new AlreadyExistsException(category.getName() + " already exists"));
    }

    @Override
    public Category updateCategory(Category category, Long id) {
        if (categoryRepository.existsByName(category.getName())
                && !getCategoryById(id).getName().equals(category.getName())) {
            throw new AlreadyExistsException(category.getName() + " already exists");
        }
        return Optional.ofNullable(getCategoryById(id)).map(oldCategory -> {
            oldCategory.setName(category.getName());
            return categoryRepository.save(oldCategory);
        }).orElseThrow(() -> new CategoryNotFoundException("Category not found!!"));
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryRepository.findById(id).ifPresentOrElse(categoryRepository::delete, () -> {
            throw new CategoryNotFoundException("Category not found!!");
        });
    }
}
