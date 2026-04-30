package com.ecommerce.admin.presentation.controller;

import com.ecommerce.admin.application.dto.AdminActionLogResponse;
import com.ecommerce.admin.application.usecase.GetAuditLogsUseCase;
import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.admin.application.usecase.GetDashboardStatsUseCase;
import com.ecommerce.admin.application.usecase.SalesAnalyticsUseCase;
import com.ecommerce.admin.presentation.dto.response.DashboardStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "System-wide statistics and audit logs for administrators")
public class AdminDashboardController {

    private final GetDashboardStatsUseCase getDashboardStatsUseCase;
    private final SalesAnalyticsUseCase salesAnalyticsUseCase;
    private final GetAuditLogsUseCase getAuditLogsUseCase;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('analytics:read')")
    @Operation(summary = "Get overall system statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(getDashboardStatsUseCase.execute()));
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('analytics:read')")
    @Operation(summary = "Get recent administrator action logs")
    public ResponseEntity<ApiResponse<List<AdminActionLogResponse>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.ok(getAuditLogsUseCase.execute()));
    }

    @GetMapping("/analytics/sales")
    @PreAuthorize("hasAuthority('analytics:read')")
    @Operation(summary = "Get daily sales analytics")
    public ResponseEntity<ApiResponse<Map<LocalDate, BigDecimal>>> getSalesAnalytics(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.ok(salesAnalyticsUseCase.getDailySales(days)));
    }
}


