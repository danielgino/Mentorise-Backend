package com.example.mentorisebackend.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utility class for date filtering and parsing operations.
 * Provides methods for converting filter strings to date ranges.
 */
public class DateFilterUtil {

    private DateFilterUtil() {
        // Prevent instantiation
    }

    /**
     * Parses a join date filter string and returns a LocalDateTime.
     * Supported filters: LAST_7_DAYS, LAST_30_DAYS, LAST_90_DAYS.
     *
     * @param filter the filter string (e.g., "LAST_7_DAYS")
     * @return a LocalDateTime representing the start of the filtered period,
     *         or null if filter is null or unrecognized
     */
    public static LocalDateTime parseJoinDateFilter(String filter) {
        if (filter == null) {
            return null;
        }

        LocalDate today = LocalDate.now();

        return switch (filter) {
            case "LAST_7_DAYS" -> today.minusDays(7).atStartOfDay();
            case "LAST_30_DAYS" -> today.minusDays(30).atStartOfDay();
            case "LAST_90_DAYS" -> today.minusDays(90).atStartOfDay();
            default -> null;
        };
    }
}

