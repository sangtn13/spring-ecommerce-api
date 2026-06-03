package com.ecommerce.sshop.controller.category;

import com.ecommerce.sshop.dto.category.CategoryDto;
import com.ecommerce.sshop.mapper.CategoryMapper;
import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.request.categories.UpsertCategoryRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.PagedResponse;
import com.ecommerce.sshop.service.category.ICategoryService;
import com.ecommerce.sshop.util.PageUtil;
import com.ecommerce.sshop.util.StringUtil;

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
    private final CategoryMapper categoryMapper;

    @GetMapping()
    public ResponseEntity<ApiResponse> getAllCategories(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        String normalizedName = StringUtil.trimToNull(name);
        if (normalizedName != null) {
            return getCategoryByName(normalizedName);
        }

        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<CategoryDto> categoryPage = categoryService.getAllCategoriesWithPaging(pageable).map(categoryMapper::toDto);
        PagedResponse<CategoryDto> pagedResponse = PagedResponse.of(categoryPage);
        return ResponseEntity.ok(new ApiResponse("Categories retrieved successfully", pagedResponse));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @PostMapping()
    public ResponseEntity<ApiResponse> addCategory(@RequestBody UpsertCategoryRequest request) {
        Category theCategory = categoryService.addCategory(request);
        return ResponseEntity.ok(new ApiResponse("Category added successfully", categoryMapper.toDto(theCategory)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable String id) {
        Category theCategory = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Category retrieved successfully", categoryMapper.toDto(theCategory)));
    }

    public ResponseEntity<ApiResponse> getCategoryByName(String name) {
        Category theCategory = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(new ApiResponse("Category retrieved successfully", categoryMapper.toDto(theCategory)));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Category deleted successfully", null));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable String id, @RequestBody UpsertCategoryRequest request) {
        Category updateCategory = categoryService.updateCategory(request, id);
        return ResponseEntity.ok(new ApiResponse("Category updated successfully", categoryMapper.toDto(updateCategory)));
    }
}
