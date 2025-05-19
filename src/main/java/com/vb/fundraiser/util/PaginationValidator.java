package com.vb.fundraiser.util;

import org.springframework.data.domain.Sort;

public class PaginationValidator {
    public static void validate(int page, int size, String direction) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
        }
    }

    public static Sort.Direction parseDirection(String sortDirection) {
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.ASC;
        }
        return direction;
    }
}
