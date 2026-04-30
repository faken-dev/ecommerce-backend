package com.ecommerce.admin.presentation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSalesStats(
    UUID productId,
    String productName,
    long totalSales,
    BigDecimal totalRevenue
) {}
