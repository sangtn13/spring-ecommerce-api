package com.ecommerce.sshop.controller.category;

import java.util.List;

import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.category.ICategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/categories")
public class CategoryController {
    private final ICategoryService categoryService;

    @GetMapping()
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponse("Categories retrieved successfully", categories));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping()
    public ResponseEntity<ApiResponse> addCategory(@RequestBody Category name) {
        Category theCategory = categoryService.addCategory(name);
        return ResponseEntity.ok(new ApiResponse("Category added successfully", theCategory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long id) {
        Category theCategory = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Category retrieved successfully", theCategory));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse> getCategoryByName(@PathVariable String name) {
        Category theCategory = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(new ApiResponse("Category retrieved successfully", theCategory));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Category deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updateCategory = categoryService.updateCategory(category, id);
        return ResponseEntity.ok(new ApiResponse("Category updated successfully", updateCategory));
    }
}
