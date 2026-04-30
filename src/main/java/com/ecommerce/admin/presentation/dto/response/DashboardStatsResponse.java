package com.ecommerce.admin.presentation.dto.response;

import java.util.List;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record DashboardStatsResponse(
    long totalUsers,
    long totalProducts,
    long todayOrders,
    BigDecimal todayRevenue,
    long pendingOrders,
    long lowStockProducts,
    long totalReviews,
    double averageRating,
    List<ProductSalesStats> topProducts
) {
}
