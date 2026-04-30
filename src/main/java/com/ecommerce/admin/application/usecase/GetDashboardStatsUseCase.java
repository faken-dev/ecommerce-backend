package com.ecommerce.admin.application.usecase;


import com.ecommerce.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import com.ecommerce.feedback.infrastructure.persistence.repository.ReviewJpaRepository;
import com.ecommerce.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ecommerce.auth.infrastructure.persistence.repository.UserJpaRepository;
import com.ecommerce.admin.presentation.dto.response.DashboardStatsResponse;
import com.ecommerce.admin.presentation.dto.response.ProductSalesStats;
import com.ecommerce.order.application.port.InventoryQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class GetDashboardStatsUseCase {

    private final UserJpaRepository userJpaRepository;
    private final ProductJpaRepository productJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final ReviewJpaRepository reviewJpaRepository;
    private final InventoryQueryPort inventoryQueryPort;

    @Transactional(readOnly = true)
    public DashboardStatsResponse execute() {
        long totalUsers = userJpaRepository.count();
        long totalProducts = productJpaRepository.count();
        
        Instant startOfToday = LocalDate.now(ZoneId.of("UTC"))
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant();
        
        long todayOrders = orderJpaRepository.countPaidOrdersAfter(startOfToday);
        BigDecimal todayRevenue = orderJpaRepository.sumTotalAmountAfter(startOfToday);
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;
        
        long pendingOrders = orderJpaRepository.countByStatusAndDeletedAtIsNull("PENDING");
        long lowStockProducts = inventoryQueryPort.countLowStock();
        
        long totalReviews = reviewJpaRepository.count();
        Double avgRating = reviewJpaRepository.getAverageRating();

        // 5. Top Selling Products
        var topProductsRaw = orderJpaRepository.findTopSellingProducts(PageRequest.of(0, 5));
        List<ProductSalesStats> topProducts = topProductsRaw.stream()
                .map(row -> new ProductSalesStats(
                        (UUID) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3]
                )).toList();
        
        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .todayOrders(todayOrders)
                .todayRevenue(todayRevenue)
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProducts)
                .totalReviews(totalReviews)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .topProducts(topProducts)
                .build();
    }
}
