package com.example.mentorisebackend.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for pageable and sorting operations.
 * Provides methods for creating Pageable objects with dynamic sorting.
 */
public class PageableUtil {

    private PageableUtil() {
        // Prevent instantiation
    }

    /**
     * Creates a Pageable object with resolved sorting from a sort string.
     * Sort string format: "fieldName,direction" (e.g., "createdAt,desc" or "name,asc")
     *
     * @param page the page number (zero-indexed)
     * @param size the page size
     * @param sort the sort string in format "fieldName,direction"
     * @param defaultSortField the default field to sort by if sort is null or blank
     * @return a Pageable object with the resolved sorting
     */
    public static Pageable resolvePageable(int page, int size, String sort, String defaultSortField) {
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            String sortField = parts[0];
            String direction = (parts.length > 1) ? parts[1].toLowerCase().trim() : "desc";

            Sort sorting = "asc".equals(direction)
                    ? Sort.by(Sort.Order.asc(sortField))
                    : Sort.by(Sort.Order.desc(sortField));

            return PageRequest.of(page, size, sorting);
        }

        // Default: sort by defaultSortField in descending order
        return PageRequest.of(page, size, Sort.by(Sort.Order.desc(defaultSortField)));
    }

    /**
     * Creates a Pageable object with default descending sort by createdAt.
     *
     * @param page the page number (zero-indexed)
     * @param size the page size
     * @return a Pageable object with default descending sort by createdAt
     */
    public static Pageable resolvePageableDefault(int page, int size) {
        return resolvePageable(page, size, null, "createdAt");
    }
}

