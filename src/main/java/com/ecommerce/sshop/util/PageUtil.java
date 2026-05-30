package com.ecommerce.sshop.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for handling pagination operations
 * Converts user-friendly 1-based page numbers to 0-based for Spring Data JPA
 */
public class PageUtil {

    /**
     * Default page size
     */
    public static final int DEFAULT_PAGE_SIZE = 5;

    /**
     * Maximum page size
     */
    public static final int MAX_PAGE_SIZE = 20;

    /**
     * Default sort field
     */
    public static final String DEFAULT_SORT_BY = "id";

    /**
     * Default sort direction
     */
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    /**
     * Create Pageable with page number begin to 1 (user-friendly)
     *
     * @param page          Page number begin to 1
     * @param size          Page size
     * @param sortBy        Sort field
     * @param sortDirection Sort direction (asc/desc)
     * @return Pageable object for Spring Data JPA (0-based)
     */
    public static Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        // Validate parameters
        validatePageParams(page, size);

        // Convert from 1-based to 0-based
        int adjustedPage = Math.max(0, page - 1);

        // Handle sort direction
        Sort.Direction direction = parseSortDirection(sortDirection);

        // Handle sort field
        String validSortBy = sortBy != null && !sortBy.trim().isEmpty() ? sortBy : DEFAULT_SORT_BY;

        return PageRequest.of(adjustedPage, size, Sort.by(direction, validSortBy));
    }

    /**
     * Create Pageable with default values
     * 
     * @param page Page number begin to 1
     * @param size Page size
     * @return Pageable object
     */
    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION);
    }

    /**
     * Create Pageable with default size and sort
     * 
     * @param page Page number begin to 1
     * @return Pageable object
     */
    public static Pageable createPageable(int page) {
        return createPageable(page, DEFAULT_PAGE_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION);
    }

    /**
     * Parse sort direction from string
     * 
     * @param sortDirection Sort direction string
     * @return Sort.Direction enum
     */
    public static Sort.Direction parseSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return Sort.Direction.ASC;
        }

        return sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    /**
     * Validate page parameters
     * 
     * @param page Page number (should be >= 1)
     * @param size Page size (should be 1-100)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static void validatePageParams(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0. Current value: " + page);
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0. Current value: " + size);
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must not exceed " + MAX_PAGE_SIZE + ". Current value: " + size);
        }
    }

    /**
     * Validate page number only
     * 
     * @param page Page number
     */
    public static void validatePageNumber(int page) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0. Current value: " + page);
        }
    }

    /**
     * Validate page size only
     * 
     * @param size Page size
     */
    public static void validatePageSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0. Current value: " + size);
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must not exceed " + MAX_PAGE_SIZE + ". Current value: " + size);
        }
    }

    /**
     * Calculate total pages
     * 
     * @param totalElements Total number of elements
     * @param pageSize      Page size
     * @return Total pages
     */
    public static int calculateTotalPages(long totalElements, int pageSize) {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    /**
     * Check if page is valid for given total elements
     * 
     * @param page          Page number (1-based)
     * @param totalElements Total elements
     * @param pageSize      Page size
     * @return true if valid
     */
    public static boolean isValidPage(int page, long totalElements, int pageSize) {
        if (page < 1)
            return false;
        if (totalElements == 0)
            return page == 1;

        int totalPages = calculateTotalPages(totalElements, pageSize);
        return page <= totalPages;
    }
}