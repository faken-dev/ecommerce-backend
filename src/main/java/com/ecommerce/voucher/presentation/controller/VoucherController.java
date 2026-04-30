package com.ecommerce.voucher.presentation.controller;


import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.voucher.application.command.ApplyVoucherCommand;
import com.ecommerce.voucher.application.command.CreateVoucherCommand;
import com.ecommerce.voucher.application.command.ValidateVoucherCommand;
import com.ecommerce.voucher.application.dto.DiscountValidationResult;
import com.ecommerce.voucher.application.dto.VoucherResponse;
import com.ecommerce.voucher.application.usecase.ApplyVoucherUseCase;
import com.ecommerce.voucher.application.usecase.CreateVoucherUseCase;
import com.ecommerce.voucher.application.usecase.DeleteVoucherUseCase;
import com.ecommerce.voucher.application.usecase.GetVoucherUseCase;
import com.ecommerce.voucher.application.usecase.UpdateVoucherUseCase;
import com.ecommerce.voucher.application.usecase.ValidateVoucherUseCase;
import com.ecommerce.voucher.application.usecase.CollectVoucherUseCase;
import com.ecommerce.voucher.application.usecase.GetMyVouchersUseCase;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import com.ecommerce.voucher.presentation.dto.request.ApplyVoucherRequest;
import com.ecommerce.voucher.presentation.dto.request.CreateVoucherRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * REST API for voucher management and application.
 *
 * <p>Public endpoints: validate voucher
 * <p>Buyer endpoints: list active vouchers, apply voucher
 * <p>Seller endpoints: create, update, list own vouchers
 * <p>Admin endpoints: manage all vouchers
 */
@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
@Tag(name = "Voucher", description = "Voucher and discount management")
public class VoucherController {

    private final CreateVoucherUseCase createVoucherUseCase;
    private final ValidateVoucherUseCase validateVoucherUseCase;
    private final ApplyVoucherUseCase applyVoucherUseCase;
    private final GetVoucherUseCase getVoucherUseCase;
    private final UpdateVoucherUseCase updateVoucherUseCase;
    private final DeleteVoucherUseCase deleteVoucherUseCase;
    private final CollectVoucherUseCase collectVoucherUseCase;
    private final GetMyVouchersUseCase getMyVouchersUseCase;

    // ──
    // PUBLIC / BUYER
    // ──

    @Operation(summary = "Validate voucher (preview discount without applying)")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<DiscountValidationResult>> validateVoucher(
            @RequestParam String code,
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) BigDecimal subtotal,
            @RequestParam(required = false) BigDecimal shippingFee,
            @RequestParam(required = false) Set<UUID> productIds,
            @RequestParam(required = false) Set<UUID> categoryIds) {

        ValidateVoucherCommand cmd = new ValidateVoucherCommand(
                code, userId, subtotal, shippingFee, productIds, categoryIds);
        return ResponseEntity.ok(ApiResponse.ok(validateVoucherUseCase.execute(cmd)));
    }

    @Operation(summary = "List all active public vouchers")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Iterable<VoucherResponse>>> listActiveVouchers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                getVoucherUseCase.listActiveVouchers(pageable)));
    }

    @Operation(summary = "Get voucher by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(getVoucherUseCase.executeByCode(code)));
    }

    @Operation(summary = "Apply voucher to an order (commit usage)")
    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('voucher:apply')")
    public ResponseEntity<ApiResponse<DiscountValidationResult>> applyVoucher(
            @Valid @RequestBody ApplyVoucherRequest req,
            @AuthenticationPrincipal UUID userId) {

        ApplyVoucherCommand cmd = new ApplyVoucherCommand(
                req.code(),
                userId,
                req.orderId(),
                req.subtotal(),
                req.shippingFee(),
                req.productIds(),
                req.categoryIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(applyVoucherUseCase.execute(cmd)));
    }

    @Operation(summary = "Collect/Save a voucher to my account")
    @PostMapping("/collect/{code}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VoucherResponse>> collectVoucher(
            @PathVariable String code,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(collectVoucherUseCase.execute(code, userId)));
    }

    @Operation(summary = "List my collected vouchers")
    @GetMapping("/my-collected")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Iterable<VoucherResponse>>> listMyCollectedVouchers(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(getMyVouchersUseCase.execute(userId, activeOnly, pageable)));
    }

    // ──
    // SELLER
    // ──

    @Operation(summary = "Create a new voucher (seller)")
    @PostMapping
    @PreAuthorize("hasAuthority('voucher:create')")
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Valid @RequestBody CreateVoucherRequest req,
            @AuthenticationPrincipal UUID sellerId) {

        CreateVoucherCommand cmd = new CreateVoucherCommand(
                req.code(),
                req.name(),
                req.description(),
                req.type(),
                req.scope(),
                req.discountValue(),
                req.maxDiscountAmount(),
                req.minOrderAmount(),
                req.maxUsageTotal() != null ? req.maxUsageTotal() : -1,
                req.maxUsagePerUser() != null ? req.maxUsagePerUser() : 1,
                req.validFrom(),
                req.validTo(),
                req.applicableProductIds(),
                req.applicableCategoryIds(),
                sellerId,
                req.requiresCollection() != null ? req.requiresCollection() : false
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(createVoucherUseCase.execute(cmd, sellerId)));
    }

    @Operation(summary = "List my vouchers (seller dashboard)")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('voucher:read')")
    public ResponseEntity<ApiResponse<Iterable<VoucherResponse>>> listMyVouchers(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UUID sellerId) {

        VoucherStatus voucherStatus = status != null ? VoucherStatus.valueOf(status) : null;
        var page = (voucherStatus != null)
                ? getVoucherUseCase.listBySellerAndStatus(sellerId, voucherStatus, pageable)
                : getVoucherUseCase.listBySeller(sellerId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @Operation(summary = "Activate a voucher (seller)")
    @PostMapping("/{voucherId}/activate")
    @PreAuthorize("hasAuthority('voucher:update')")
    public ResponseEntity<ApiResponse<VoucherResponse>> activateVoucher(
            @PathVariable UUID voucherId,
            @AuthenticationPrincipal UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                updateVoucherUseCase.activate(voucherId, sellerId)));
    }

    @Operation(summary = "Disable a voucher (seller/admin)")
    @PostMapping("/{voucherId}/disable")
    @PreAuthorize("hasAuthority('voucher:update')")
    public ResponseEntity<ApiResponse<VoucherResponse>> disableVoucher(
            @PathVariable UUID voucherId,
            @AuthenticationPrincipal UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                updateVoucherUseCase.disable(voucherId, sellerId)));
    }

    // ──
    // ADMIN
    // ──

    @Operation(summary = "List all vouchers (admin)")
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('voucher:manage')")
    public ResponseEntity<ApiResponse<Iterable<VoucherResponse>>> listAllVouchers(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {

        VoucherStatus voucherStatus = status != null ? VoucherStatus.valueOf(status) : null;
        return ResponseEntity.ok(ApiResponse.ok(
                getVoucherUseCase.listAll(voucherStatus, pageable)));
    }

    @Operation(summary = "Get voucher by ID (admin)")
    @GetMapping("/{voucherId}")
    @PreAuthorize("hasAuthority('voucher:read')")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucher(@PathVariable UUID voucherId) {
        return ResponseEntity.ok(ApiResponse.ok(getVoucherUseCase.execute(voucherId)));
    }

    @Operation(summary = "Expire a voucher (admin)")
    @PostMapping("/{voucherId}/expire")
    @PreAuthorize("hasAuthority('voucher:manage')")
    public ResponseEntity<ApiResponse<VoucherResponse>> expireVoucher(
            @PathVariable UUID voucherId,
            @AuthenticationPrincipal UUID adminId) {
        return ResponseEntity.ok(ApiResponse.ok(
                updateVoucherUseCase.expire(voucherId, adminId)));
    }

    @Operation(summary = "Delete a voucher (admin)")
    @DeleteMapping("/{voucherId}")
    @PreAuthorize("hasAuthority('voucher:manage')")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable UUID voucherId) {
        deleteVoucherUseCase.execute(voucherId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Voucher deleted successfully"));
    }
}
