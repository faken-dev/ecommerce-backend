package com.ecommerce.order.application.dto;

import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class SellerDashboardStatsResponse {
    private final long totalProducts;
    private final long pendingOrders;
    private final long monthlyOrders;
    private final BigDecimal monthlyRevenue;
    private final double monthlyOrdersDelta;
    private final double monthlyRevenueDelta;

    public SellerDashboardStatsResponse(long totalProducts, long pendingOrders, long monthlyOrders, BigDecimal monthlyRevenue, double monthlyOrdersDelta, double monthlyRevenueDelta) {
        this.totalProducts = totalProducts;
        this.pendingOrders = pendingOrders;
        this.monthlyOrders = monthlyOrders;
        this.monthlyRevenue = monthlyRevenue;
        this.monthlyOrdersDelta = monthlyOrdersDelta;
        this.monthlyRevenueDelta = monthlyRevenueDelta;
    }

    public long getTotalProducts() { return totalProducts; }
    public long getPendingOrders() { return pendingOrders; }
    public long getMonthlyOrders() { return monthlyOrders; }
    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public double getMonthlyOrdersDelta() { return monthlyOrdersDelta; }
    public double getMonthlyRevenueDelta() { return monthlyRevenueDelta; }

    public static SellerDashboardStatsResponseBuilder builder() {
        return new SellerDashboardStatsResponseBuilder();
    }

    public static class SellerDashboardStatsResponseBuilder {
        private long totalProducts;
        private long pendingOrders;
        private long monthlyOrders;
        private BigDecimal monthlyRevenue;
        private double monthlyOrdersDelta;
        private double monthlyRevenueDelta;

        public SellerDashboardStatsResponseBuilder totalProducts(long totalProducts) { this.totalProducts = totalProducts; return this; }
        public SellerDashboardStatsResponseBuilder pendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; return this; }
        public SellerDashboardStatsResponseBuilder monthlyOrders(long monthlyOrders) { this.monthlyOrders = monthlyOrders; return this; }
        public SellerDashboardStatsResponseBuilder monthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; return this; }
        public SellerDashboardStatsResponseBuilder monthlyOrdersDelta(double monthlyOrdersDelta) { this.monthlyOrdersDelta = monthlyOrdersDelta; return this; }
        public SellerDashboardStatsResponseBuilder monthlyRevenueDelta(double monthlyRevenueDelta) { this.monthlyRevenueDelta = monthlyRevenueDelta; return this; }

        public SellerDashboardStatsResponse build() {
            return new SellerDashboardStatsResponse(totalProducts, pendingOrders, monthlyOrders, monthlyRevenue, monthlyOrdersDelta, monthlyRevenueDelta);
        }
    }
}
