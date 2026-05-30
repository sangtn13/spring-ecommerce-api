package com.ecommerce.sshop.controller.category;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.category.ICategoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock private ICategoryService categoryService;

    @InjectMocks private CategoryController categoryController;

    private Category sampleCategory;
    private final String categoryId = "cat-uuid-123";
    private final String categoryName = "Electronics";

    @BeforeEach
    void setUp() {
        sampleCategory = new Category();
        sampleCategory.setId(categoryId);
        sampleCategory.setName(categoryName);
    }

    @Test
    @DisplayName("Get all categories with pagination successfully")
    void getAllCategories_Success() {
        Page<Category> page = new PageImpl<>(List.of(sampleCategory));
        when(categoryService.getAllCategoriesWithPaging(any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse> response = categoryController.getAllCategories(1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Categories retrieved successfully", response.getBody().getMessage());
        verify(categoryService, times(1)).getAllCategoriesWithPaging(any(Pageable.class));
    }

    @Test
    @DisplayName("Add new category successfully (Admin)")
    void addCategory_Success() {
        when(categoryService.addCategory(any(Category.class))).thenReturn(sampleCategory);

        ResponseEntity<ApiResponse> response = categoryController.addCategory(sampleCategory);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Category added successfully", response.getBody().getMessage());
        assertEquals(sampleCategory, response.getBody().getData());
    }

    @Test
    @DisplayName("Get category by ID successfully")
    void getCategoryById_Success() {
        when(categoryService.getCategoryById(categoryId)).thenReturn(sampleCategory);

        ResponseEntity<ApiResponse> response = categoryController.getCategoryById(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Category retrieved successfully", response.getBody().getMessage());
        assertEquals(sampleCategory, response.getBody().getData());
    }

    @Test
    @DisplayName("Get category by name successfully")
    void getCategoryByName_Success() {
        when(categoryService.getCategoryByName(categoryName)).thenReturn(sampleCategory);

        ResponseEntity<ApiResponse> response = categoryController.getCategoryByName(categoryName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleCategory, response.getBody().getData());
    }

    @Test
    @DisplayName("Delete category by ID successfully (Admin)")
    void deleteCategory_Success() {
        doNothing().when(categoryService).deleteCategoryById(categoryId);

        ResponseEntity<ApiResponse> response = categoryController.deleteCategory(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Category deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(categoryService, times(1)).deleteCategoryById(categoryId);
    }

    @Test
    @DisplayName("Update category successfully (Admin)")
    void updateCategory_Success() {
        Category updateData = new Category("New Electronics");
        when(categoryService.updateCategory(any(Category.class), eq(categoryId))).thenReturn(sampleCategory);

        ResponseEntity<ApiResponse> response = categoryController.updateCategory(categoryId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Category updated successfully", response.getBody().getMessage());
        verify(categoryService, times(1)).updateCategory(any(Category.class), eq(categoryId));
    }
}