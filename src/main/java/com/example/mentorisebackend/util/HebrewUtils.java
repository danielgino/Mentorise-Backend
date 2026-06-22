package com.example.mentorisebackend.util;

/**
 * Utility class for Hebrew language-related operations.
 * Provides methods for converting numbers to Hebrew text representations.
 */
public class HebrewUtils {

    private HebrewUtils() {
        // Prevent instantiation
    }

    /**
     * Converts a year number (1-6) to its Hebrew representation.
     *
     * @param year the year number (1-6)
     * @return the Hebrew year label (e.g., "שנה א׳", "שנה ב׳")
     */
    public static String getHebrewYearLabel(Integer year) {
        if (year == null) {
            return "שנה לא ידועה";
        }

        return switch (year) {
            case 1 -> "שנה א׳";
            case 2 -> "שנה ב׳";
            case 3 -> "שנה ג׳";
            case 4 -> "שנה ד׳";
            case 5 -> "שנה ה׳";
            case 6 -> "שנה ו׳";
            default -> "שנה " + year;
        };
    }
}

