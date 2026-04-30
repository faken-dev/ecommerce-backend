package com.ecommerce.catalog.presentation.dto.request;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductSearchCriteria(
    String query,
    UUID categoryId,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Double minRating,
    Boolean inStock,
    String sortBy, // price_asc, price_desc, rating, newest
    Integer page,
    Integer size
) {}
