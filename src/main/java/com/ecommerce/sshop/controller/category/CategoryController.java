package com.ecommerce.sshop.controller.category;

import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.PagedResponse;
import com.ecommerce.sshop.service.category.ICategoryService;
import com.ecommerce.sshop.util.PageUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/categories")
public class CategoryController {
    private final ICategoryService categoryService;

    @GetMapping()
    public ResponseEntity<ApiResponse> getAllCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Category> categoryPage = categoryService.getAllCategoriesWithPaging(pageable);
        PagedResponse<Category> pagedResponse = PagedResponse.of(categoryPage);
        return ResponseEntity.ok(new ApiResponse("Categories retrieved successfully", pagedResponse));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping()
    public ResponseEntity<ApiResponse> addCategory(@RequestBody Category name) {
        Category theCategory = categoryService.addCategory(name);
        return ResponseEntity.ok(new ApiResponse("Category added successfully", theCategory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable String id) {
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
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Category deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable String id, @RequestBody Category category) {
        Category updateCategory = categoryService.updateCategory(category, id);
        return ResponseEntity.ok(new ApiResponse("Category updated successfully", updateCategory));
    }
}
