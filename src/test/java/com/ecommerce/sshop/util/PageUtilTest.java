package com.ecommerce.sshop.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageUtilTest {

    @Test
    @DisplayName("Initialize Pageable successfully with all parameters")
    void createPageable_AllParams_Success() {
        Pageable pageable = PageUtil.createPageable(2, 10, "name", "desc");
        assertNotNull(pageable);
        assertEquals(1, pageable.getPageNumber()); // 2 - 1 = 1 (0-based index)
        assertEquals(10, pageable.getPageSize());
        assertTrue(pageable.getSort().getOrderFor("name").isDescending());
    }

    @Test
    @DisplayName("Initialize Pageable successfully with default parameters")
    void createPageable_DefaultParams_Success() {
        Pageable p1 = PageUtil.createPageable(1, 5);
        assertEquals(0, p1.getPageNumber());
        assertEquals(5, p1.getPageSize());

        Pageable p2 = PageUtil.createPageable(3);
        assertEquals(2, p2.getPageNumber());
        assertEquals(5, p2.getPageSize()); // DEFAULT_PAGE_SIZE = 5
    }

    @Test
    @DisplayName("Throw IllegalArgumentException when page number is invalid (< 1)")
    void validatePageParams_InvalidPage_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> PageUtil.createPageable(0, 5));
        assertThrows(IllegalArgumentException.class, () -> PageUtil.validatePageNumber(0));
    }

    @Test
    @DisplayName("Throw IllegalArgumentException when page size is invalid (<= 0 or too large)")
    void validatePageParams_InvalidSize_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> PageUtil.createPageable(1, 0));
        assertThrows(IllegalArgumentException.class, () -> PageUtil.createPageable(1, 100)); // Vượt quá MAX_PAGE_SIZE (20)
        assertThrows(IllegalArgumentException.class, () -> PageUtil.validatePageSize(0));
        assertThrows(IllegalArgumentException.class, () -> PageUtil.validatePageSize(25));
    }

    @Test
    @DisplayName("Parse sort direction correctly with various inputs")
    void parseSortDirection_Checks() {
        assertEquals(Sort.Direction.ASC, PageUtil.parseSortDirection(null));
        assertEquals(Sort.Direction.ASC, PageUtil.parseSortDirection(""));
        assertEquals(Sort.Direction.DESC, PageUtil.parseSortDirection("desc"));
        assertEquals(Sort.Direction.ASC, PageUtil.parseSortDirection("xyz"));
    }

    @Test
    @DisplayName("Calculate total pages and validate current page")
    void paginationCalculations_Success() {
        assertEquals(3, PageUtil.calculateTotalPages(15, 5));
        assertEquals(4, PageUtil.calculateTotalPages(16, 5));

        assertTrue(PageUtil.isValidPage(1, 15, 5));
        assertFalse(PageUtil.isValidPage(0, 15, 5));
        assertFalse(PageUtil.isValidPage(5, 15, 5));
        assertTrue(PageUtil.isValidPage(1, 0, 5)); // Trường hợp danh sách trống
    }
}