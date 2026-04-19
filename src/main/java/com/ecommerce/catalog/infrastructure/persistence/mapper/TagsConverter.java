package com.ecommerce.catalog.infrastructure.persistence.mapper;

import java.util.List;

/** Static utilities for converting tags between JPA TEXT (comma-separated) and domain List. */
public final class TagsConverter {

    private TagsConverter() {}

    public static String listToString(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "";
        return String.join(",", tags);
    }

    public static List<String> stringToList(String tags) {
        if (tags == null || tags.isBlank()) return List.of();
        return List.of(tags.split(","));
    }
}