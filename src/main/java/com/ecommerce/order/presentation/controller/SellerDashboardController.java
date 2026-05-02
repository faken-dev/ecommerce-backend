package com.ecommerce.order.presentation.controller;

import com.ecommerce.admin.application.usecase.SalesAnalyticsUseCase;
import com.ecommerce.order.application.dto.SellerDashboardStatsResponse;
import com.ecommerce.order.application.usecase.GetSellerDashboardStatsUseCase;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/dashboard")
@RequiredArgsConstructor
@Tag(name = "Seller Dashboard", description = "Statistics for sellers")
public class SellerDashboardController {

    private final GetSellerDashboardStatsUseCase getSellerDashboardStatsUseCase;
    private final SalesAnalyticsUseCase salesAnalyticsUseCase;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('seller:dashboard')")
    @Operation(summary = "Get seller dashboard statistics")
    public ResponseEntity<ApiResponse<SellerDashboardStatsResponse>> getStats(
            @AuthenticationPrincipal(expression = "id") UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(getSellerDashboardStatsUseCase.execute(sellerId)));
    }

    @GetMapping("/analytics/sales")
    @PreAuthorize("hasAuthority('seller:analytics')")
    @Operation(summary = "Get seller sales analytics")
    public ResponseEntity<ApiResponse<java.util.Map<java.time.LocalDate, java.math.BigDecimal>>> getSalesAnalytics(
            @AuthenticationPrincipal(expression = "id") UUID sellerId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.ok(salesAnalyticsUseCase.getDailySalesForSeller(sellerId, days)));
    }
}


