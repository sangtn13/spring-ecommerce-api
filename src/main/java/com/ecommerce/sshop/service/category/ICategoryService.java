package com.ecommerce.sshop.service.category;

import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.request.categories.UpsertCategoryRequest;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICategoryService {
    Category getCategoryById(String id);

    Category getCategoryByName(String name);

    List<Category> getAllCategories();

    Category addCategory(UpsertCategoryRequest request);

    Category updateCategory(UpsertCategoryRequest request, String id);

    void deleteCategoryById(String id);

    Page<Category> getAllCategoriesWithPaging(Pageable pageable);

}
