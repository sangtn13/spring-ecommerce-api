package com.ecommerce.sshop.service.category;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.exception.category.CategoryNotFoundException;
import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.model.category.Category;
import com.ecommerce.sshop.repository.category.ICategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private ICategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category sampleCategory;
    private final String categoryId = "cat-uuid-123";
    private final String categoryName = "Electronics";

    @BeforeEach
    void setUp() {
        sampleCategory = new Category();
        sampleCategory.setId(categoryId);
        sampleCategory.setName(categoryName);
    }

    // ==========================================
    // TEST GET CATEGORY BY ID
    // ==========================================
    @Test
    @DisplayName("Find Category by ID successfully")
    void getCategoryById_Success() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));

        Category result = categoryService.getCategoryById(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        assertEquals(categoryName, result.getName());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("Find Category by ID failed - Throws CategoryNotFoundException")
    void getCategoryById_NotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.getCategoryById(categoryId);
        });

        assertEquals("Category not found!!", exception.getMessage());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    // ==========================================
    // TEST GET CATEGORY BY NAME
    // ==========================================
    @Test
    @DisplayName("Find Category by name successfully")
    void getCategoryByName_Success() {
        when(categoryRepository.findByName(categoryName)).thenReturn(sampleCategory);

        Category result = categoryService.getCategoryByName(categoryName);

        assertNotNull(result);
        assertEquals(categoryName, result.getName());
        verify(categoryRepository, times(1)).findByName(categoryName);
    }

    // ==========================================
    // TEST GET ALL CATEGORIES
    // ==========================================
    @Test
    @DisplayName("Get all categories successfully")
    void getAllCategories_Success() {
        List<Category> list = new ArrayList<>();
        list.add(sampleCategory);
        when(categoryRepository.findAll()).thenReturn(list);

        List<Category> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(categoryRepository, times(1)).findAll();
    }

    // ==========================================
    // TEST ADD CATEGORY
    // ==========================================
    @Test
    @DisplayName("Add new Category successfully when name does not exist")
    void addCategory_Success() {
        when(categoryRepository.existsByName(categoryName)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);

        Category savedCategory = categoryService.addCategory(sampleCategory);

        assertNotNull(savedCategory);
        assertEquals(categoryName, savedCategory.getName());
        verify(categoryRepository, times(1)).existsByName(categoryName);
        verify(categoryRepository, times(1)).save(sampleCategory);
    }

    @Test
    @DisplayName("Add new Category failed - Name already exists, throws AlreadyExistsException")
    void addCategory_AlreadyExists() {
        when(categoryRepository.existsByName(categoryName)).thenReturn(true);

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> {
            categoryService.addCategory(sampleCategory);
        });

        assertEquals(categoryName + " already exists", exception.getMessage());
        verify(categoryRepository, times(1)).existsByName(categoryName);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ==========================================
    // TEST UPDATE CATEGORY
    // ==========================================
    @Test
    @DisplayName("Update Category failed - New name is already used by another id, throws AlreadyExistsException")
    void updateCategory_NameAlreadyExistsForOtherId() {
        String newName = "Gadgets";
        Category updateRequest = new Category();
        updateRequest.setName(newName);

        // Mock: New name already exists in the DB
        when(categoryRepository.existsByName(newName)).thenReturn(true);
        // Mock: Current record has a different name (Electronics) -> Cross-name
        // duplication
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> {
            categoryService.updateCategory(updateRequest, categoryId);
        });

        assertEquals(newName + " already exists", exception.getMessage());
        verify(categoryRepository, times(1)).existsByName(newName);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Update Category successfully - Keep the same name or change to a completely new name that is not used by anyone")
    void updateCategory_Success() {
        String newName = "New Electronics";
        Category updateRequest = new Category();
        updateRequest.setName(newName);

        when(categoryRepository.existsByName(newName)).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategory(updateRequest, categoryId);

        assertNotNull(result);
        assertEquals(newName, result.getName());
        verify(categoryRepository, times(1)).save(sampleCategory);
    }

    // ==========================================
    // TEST DELETE CATEGORY BY ID
    // ==========================================
    @Test
    @DisplayName("Delete Category successfully when record is found")
    void deleteCategoryById_Success() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));
        doNothing().when(categoryRepository).delete(sampleCategory);

        assertDoesNotThrow(() -> categoryService.deleteCategoryById(categoryId));

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).delete(sampleCategory);
    }

    @Test
    @DisplayName("Delete Category failed - ID not found, throws CategoryNotFoundException")
    void deleteCategoryById_NotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.deleteCategoryById(categoryId);
        });

        assertEquals("Category not found!!", exception.getMessage());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // ==========================================
    // TEST GET ALL CATEGORIES WITH PAGING
    // ==========================================
    @Test
    @DisplayName("Get all categories with paging successfully")
    void getAllCategoriesWithPaging_Success() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Category> list = List.of(sampleCategory);
        Page<Category> mockPage = new PageImpl<>(list, pageable, list.size());

        when(categoryRepository.findAll(pageable)).thenReturn(mockPage);

        Page<Category> result = categoryService.getAllCategoriesWithPaging(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(categoryName, result.getContent().get(0).getName());
        verify(categoryRepository, times(1)).findAll(pageable);
    }
}