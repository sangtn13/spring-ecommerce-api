package com.ecommerce.sshop.service.category;

import com.ecommerce.sshop.model.category.Category;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICategoryService {
    Category getCategoryById(Long id);

    Category getCategoryByName(String name);

    List<Category> getAllCategories();

    Category addCategory(Category category);

    Category updateCategory(Category category, Long id);

    void deleteCategoryById(Long id);

    Page<Category> getAllCategoriesWithPaging(Pageable pageable);

}
