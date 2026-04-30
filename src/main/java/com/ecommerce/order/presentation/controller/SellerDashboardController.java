package com.ecommerce.order.presentation.controller;

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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/dashboard")
@RequiredArgsConstructor
@Tag(name = "Seller Dashboard", description = "Statistics for sellers")
public class SellerDashboardController {

    private final GetSellerDashboardStatsUseCase getSellerDashboardStatsUseCase;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @Operation(summary = "Get seller dashboard statistics")
    public ResponseEntity<ApiResponse<SellerDashboardStatsResponse>> getStats(
            @AuthenticationPrincipal UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(getSellerDashboardStatsUseCase.execute(sellerId)));
    }
}


