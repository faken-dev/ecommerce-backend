package com.ecommerce.voucher.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;
import com.ecommerce.voucher.domain.enums.VoucherScope;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import com.ecommerce.voucher.domain.enums.VoucherType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor
@SuperBuilder
public class Voucher extends AuditableEntity {

    private String code;
    private String name;
    private String description;
    private VoucherType type;
    private VoucherScope scope;
    private VoucherStatus status;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private int maxUsageTotal;
    private int maxUsagePerUser;
    private Instant validFrom;
    private Instant validTo;
    private Set<UUID> applicableProductIds;
    private Set<UUID> applicableCategoryIds;
    private UUID sellerId;
    private int currentUsageCount;
    private Instant expiredAt;
    private Instant deletedAt;
    private boolean requiresCollection;

    public static Voucher create(
            String code,
            String name,
            VoucherType type,
            VoucherScope scope,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            int maxUsageTotal,
            int maxUsagePerUser,
            Instant validFrom,
            Instant validTo,
            UUID sellerId) {

        validateCode(code);
        validateDiscountValue(type, discountValue);

        return Voucher.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .code(code.toUpperCase().trim())
                .name(name.trim())
                .type(type)
                .scope(scope)
                .status(VoucherStatus.ACTIVE)
                .discountValue(discountValue)
                .minOrderAmount(minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO)
                .maxUsageTotal(maxUsageTotal)
                .maxUsagePerUser(maxUsagePerUser)
                .validFrom(validFrom)
                .validTo(validTo)
                .applicableProductIds(new HashSet<>())
                .applicableCategoryIds(new HashSet<>())
                .sellerId(sellerId)
                .currentUsageCount(0)
                .requiresCollection(false)
                .build();
    }

    public void activate() {
        if (this.status != VoucherStatus.PENDING && this.status != VoucherStatus.DISABLED) {
            throw new BusinessException(ErrorCode.VOUCHER_INVALID_STATUS_TRANSITION,
                    "Voucher can only be activated from PENDING or DISABLED status");
        }
        this.status = VoucherStatus.ACTIVE;
        this.touchUpdate();
    }

    public void disable() {
        if (this.status == VoucherStatus.EXPIRED || this.status == VoucherStatus.DEPLETED) {
            throw new BusinessException(ErrorCode.VOUCHER_INVALID_STATUS_TRANSITION,
                    "Cannot disable a voucher in terminal status");
        }
        this.status = VoucherStatus.DISABLED;
        this.touchUpdate();
    }

    public void markAsExpired() {
        if (this.status == VoucherStatus.DEPLETED || this.status == VoucherStatus.EXPIRED) {
            return;
        }
        this.status = VoucherStatus.EXPIRED;
        this.expiredAt = Instant.now();
        this.touchUpdate();
    }

    public void markAsDepleted() {
        if (this.status == VoucherStatus.EXPIRED || this.status == VoucherStatus.DEPLETED) {
            return;
        }
        this.status = VoucherStatus.DEPLETED;
        this.touchUpdate();
    }

    public DiscountResult calculateDiscount(
            BigDecimal subtotal,
            BigDecimal shippingFee,
            Set<UUID> productIds,
            Set<UUID> categoryIds,
            int userUsageCount) {

        if (!isActive()) {
            return DiscountResult.notApplicable("Voucher is not active");
        }
        if (!isWithinValidityPeriod()) {
            return DiscountResult.notApplicable("Voucher is outside validity period");
        }
        if (isUsageExhausted()) {
            return DiscountResult.notApplicable("Voucher usage limit exhausted");
        }
        if (isUserUsageExhausted(userUsageCount)) {
            return DiscountResult.notApplicable("User usage limit reached for this voucher");
        }

        if (minOrderAmount != null && subtotal.compareTo(minOrderAmount) < 0) {
            return DiscountResult.notApplicable(
                    "Minimum order amount is %s".formatted(minOrderAmount));
        }

        if (scope == VoucherScope.SPECIFIC_PRODUCTS && !isApplicableToProducts(productIds)) {
            return DiscountResult.notApplicable("No applicable products in cart");
        }
        if (scope == VoucherScope.SPECIFIC_CATEGORIES && !isApplicableToCategories(categoryIds)) {
            return DiscountResult.notApplicable("No applicable categories in cart");
        }

        BigDecimal discountAmount;
        String discountDescription;

        switch (type) {
            case PERCENTAGE -> {
                BigDecimal rawDiscount = subtotal
                        .multiply(discountValue)
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                discountAmount = maxDiscountAmount != null
                        ? rawDiscount.min(maxDiscountAmount)
                        : rawDiscount;
                discountDescription = "%s%% off".formatted(discountValue);
            }
            case FIXED_AMOUNT -> {
                discountAmount = discountValue.min(subtotal);
                discountDescription = "%s VND off".formatted(discountValue);
            }
            case FREE_SHIPPING -> {
                discountAmount = discountValue != null
                        ? discountValue.min(shippingFee != null ? shippingFee : BigDecimal.ZERO)
                        : (shippingFee != null ? shippingFee : BigDecimal.ZERO);
                discountDescription = "Free shipping";
            }
            case BUY_X_GET_Y -> {
                discountAmount = BigDecimal.ZERO;
                discountDescription = "Buy %s Get Y free".formatted(discountValue.intValue());
            }
            default -> {
                discountAmount = BigDecimal.ZERO;
                discountDescription = "Unknown voucher type";
            }
        }

        return DiscountResult.applicable(discountAmount, discountDescription);
    }

    public boolean isActive() {
        return status == VoucherStatus.ACTIVE;
    }

    public boolean isWithinValidityPeriod() {
        Instant now = Instant.now();
        boolean afterStart = validFrom == null || !now.isBefore(validFrom);
        boolean beforeEnd = validTo == null || !now.isAfter(validTo);
        return afterStart && beforeEnd;
    }

    public boolean isUsageExhausted() {
        return maxUsageTotal > 0 && currentUsageCount >= maxUsageTotal;
    }

    public boolean isUserUsageExhausted(int userUsageCount) {
        return maxUsagePerUser > 0 && userUsageCount >= maxUsagePerUser;
    }

    public boolean isApplicableToProducts(Set<UUID> productIds) {
        if (scope != VoucherScope.SPECIFIC_PRODUCTS) return true;
        if (applicableProductIds == null || applicableProductIds.isEmpty()) return false;
        return productIds != null && !productIds.isEmpty()
                && productIds.stream().anyMatch(applicableProductIds::contains);
    }

    public boolean isApplicableToCategories(Set<UUID> categoryIds) {
        if (scope != VoucherScope.SPECIFIC_CATEGORIES) return true;
        if (applicableCategoryIds == null || applicableCategoryIds.isEmpty()) return false;
        return categoryIds != null && !categoryIds.isEmpty()
                && categoryIds.stream().anyMatch(applicableCategoryIds::contains);
    }

    public void incrementUsage() {
        this.currentUsageCount++;
        this.touchUpdate();
        if (maxUsageTotal > 0 && currentUsageCount >= maxUsageTotal) {
            markAsDepleted();
        }
    }

    public void decrementUsage() {
        if (this.currentUsageCount > 0) {
            this.currentUsageCount--;
            this.touchUpdate();
        }
        if (this.status == VoucherStatus.DEPLETED && currentUsageCount < maxUsageTotal) {
            this.status = VoucherStatus.ACTIVE;
        }
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Voucher code is required");
        }
        if (code.length() < 3 || code.length() > 50) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Voucher code must be between 3 and 50 characters");
        }
        if (!code.matches("[A-Za-z0-9_-]+")) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Voucher code must contain only letters, numbers, hyphens and underscores");
        }
    }

    private static void validateDiscountValue(VoucherType type, BigDecimal discountValue) {
        if (discountValue == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Discount value is required");
        }
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Discount value must be positive");
        }
        switch (type) {
            case PERCENTAGE -> {
                if (discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                            "Percentage discount cannot exceed 100%");
                }
            }
            case FIXED_AMOUNT, FREE_SHIPPING -> {
                if (discountValue.compareTo(BigDecimal.valueOf(1_000_000_000)) > 0) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                            "Fixed discount amount is unreasonably large");
                }
            }
            default -> {}
        }
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.touchUpdate();
    }

    @Getter
    @Setter
    public static class DiscountResult {
        private final boolean applicable;
        private final BigDecimal discountAmount;
        private final String reason;
        private final String description;

        private DiscountResult(boolean applicable, BigDecimal discountAmount,
                                String reason, String description) {
            this.applicable = applicable;
            this.discountAmount = discountAmount;
            this.reason = reason;
            this.description = description;
        }

        public static DiscountResult applicable(BigDecimal amount, String description) {
            return new DiscountResult(true, amount, null, description);
        }

        public static DiscountResult notApplicable(String reason) {
            return new DiscountResult(false, BigDecimal.ZERO, reason, null);
        }
    }

    public boolean isRequiresCollection() { return requiresCollection; }
}
