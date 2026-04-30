package com.ecommerce.order.application.usecase;

import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.order.application.dto.SellerDashboardStatsResponse;
import com.ecommerce.order.domain.entity.OrderStatus;
import com.ecommerce.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSellerDashboardStatsUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public SellerDashboardStatsResponse execute(UUID sellerId) {
        // 1. Total Products
        long totalProducts = productRepository.countBySeller(sellerId);

        // 2. Pending Orders
        long pendingOrders = orderRepository.countBySellerAndStatus(sellerId, OrderStatus.PENDING);

        // 3. Monthly Stats
        LocalDate now = LocalDate.now();
        Instant startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfPrevMonth = now.minusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        long monthlyOrders = orderRepository.countBySellerAndCreatedAtAfter(sellerId, startOfMonth);
        BigDecimal monthlyRevenue = orderRepository.sumTotalAmountBySellerAfter(sellerId, startOfMonth);
        if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;

        // Deltas (Simplified: compared to previous full month)
        long prevMonthlyOrders = orderRepository.countBySellerAndCreatedAtAfter(sellerId, startOfPrevMonth) - monthlyOrders;
        BigDecimal prevMonthlyRevenue = orderRepository.sumTotalAmountBySellerAfter(sellerId, startOfPrevMonth);
        if (prevMonthlyRevenue == null) prevMonthlyRevenue = BigDecimal.ZERO;
        prevMonthlyRevenue = prevMonthlyRevenue.subtract(monthlyRevenue);

        double ordersDelta = calculateDelta(monthlyOrders, prevMonthlyOrders);
        double revenueDelta = calculateDelta(monthlyRevenue.doubleValue(), prevMonthlyRevenue.doubleValue());

        return SellerDashboardStatsResponse.builder()
                .totalProducts(totalProducts)
                .pendingOrders(pendingOrders)
                .monthlyOrders(monthlyOrders)
                .monthlyRevenue(monthlyRevenue)
                .monthlyOrdersDelta(ordersDelta)
                .monthlyRevenueDelta(revenueDelta)
                .build();
    }

    private double calculateDelta(double current, double previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((current - previous) / previous) * 100.0;
    }
}
