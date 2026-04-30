package com.ecommerce.voucher.infrastructure.persistence.mapper;

import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.entity.UserVoucher;
import com.ecommerce.voucher.domain.entity.VoucherUsage;
import com.ecommerce.voucher.domain.enums.VoucherScope;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import com.ecommerce.voucher.domain.enums.VoucherType;
import com.ecommerce.voucher.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ecommerce.voucher.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ecommerce.voucher.infrastructure.persistence.entity.VoucherUsageJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Mapper between domain Voucher/VoucherUsage entities and JPA entities.
 */
@Component
public class VoucherDomainMapper {

    // ── Voucher ──

    public Voucher toDomain(VoucherJpaEntity jpa) {
        if (jpa == null) return null;

        return Voucher.builder()
                .id(jpa.getId())
                .code(jpa.getCode())
                .name(jpa.getName())
                .description(jpa.getDescription())
                .type(VoucherType.valueOf(jpa.getType()))
                .scope(parseScope(jpa.getScope()))
                .status(VoucherStatus.valueOf(jpa.getStatus()))
                .discountValue(jpa.getDiscountValue())
                .maxDiscountAmount(jpa.getMaxDiscountAmount())
                .minOrderAmount(jpa.getMinOrderAmount())
                .maxUsageTotal(jpa.getMaxUsageTotal())
                .maxUsagePerUser(jpa.getMaxUsagePerUser())
                .validFrom(jpa.getValidFrom())
                .validTo(jpa.getValidTo())
                .applicableProductIds(jpa.getApplicableProductIdsAsSet())
                .applicableCategoryIds(jpa.getApplicableCategoryIdsAsSet())
                .sellerId(jpa.getSellerId())
                .currentUsageCount(jpa.getCurrentUsageCount())
                .expiredAt(jpa.getExpiredAt())
                .deletedAt(jpa.getDeletedAt())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .createdBy(jpa.getCreatedBy())
                .updatedBy(jpa.getUpdatedBy())
                .requiresCollection(jpa.isRequiresCollection())
                .build();
    }

    public VoucherJpaEntity toJpa(Voucher voucher) {
        if (voucher == null) return null;

        return VoucherJpaEntity.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .type(voucher.getType().name())
                .scope(voucher.getScope().name())
                .status(voucher.getStatus().name())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minOrderAmount(voucher.getMinOrderAmount())
                .maxUsageTotal(voucher.getMaxUsageTotal())
                .maxUsagePerUser(voucher.getMaxUsagePerUser())
                .validFrom(voucher.getValidFrom())
                .validTo(voucher.getValidTo())
                .applicableProductIds(toList(voucher.getApplicableProductIds()))
                .applicableCategoryIds(toList(voucher.getApplicableCategoryIds()))
                .sellerId(voucher.getSellerId())
                .currentUsageCount(voucher.getCurrentUsageCount())
                .expiredAt(voucher.getExpiredAt())
                .deletedAt(voucher.getDeletedAt())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .createdBy(voucher.getCreatedBy())
                .updatedBy(voucher.getUpdatedBy())
                .requiresCollection(voucher.isRequiresCollection())
                .build();
    }

    /**
     * Copies only scalar fields from domain to existing JPA entity.
     * Does NOT update product/category ID lists ── handled separately.
     */
    public void copyScalarFieldsFrom(Voucher source, VoucherJpaEntity target) {
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setType(source.getType().name());
        target.setScope(source.getScope().name());
        target.setStatus(source.getStatus().name());
        target.setDiscountValue(source.getDiscountValue());
        target.setMaxDiscountAmount(source.getMaxDiscountAmount());
        target.setMinOrderAmount(source.getMinOrderAmount());
        target.setMaxUsageTotal(source.getMaxUsageTotal());
        target.setMaxUsagePerUser(source.getMaxUsagePerUser());
        target.setValidFrom(source.getValidFrom());
        target.setValidTo(source.getValidTo());
        target.setApplicableProductIds(toList(source.getApplicableProductIds()));
        target.setApplicableCategoryIds(toList(source.getApplicableCategoryIds()));
        target.setSellerId(source.getSellerId());
        target.setCurrentUsageCount(source.getCurrentUsageCount());
        target.setExpiredAt(source.getExpiredAt());
        target.setDeletedAt(source.getDeletedAt());
        target.setRequiresCollection(source.isRequiresCollection());
    }

    // ── VoucherUsage ──

    public VoucherUsage toDomain(VoucherUsageJpaEntity jpa) {
        if (jpa == null) return null;

        return VoucherUsage.builder()
                .id(jpa.getId())
                .voucherId(jpa.getVoucher().getId())
                .userId(jpa.getUserId())
                .orderId(jpa.getOrderId())
                .discountApplied(jpa.getDiscountApplied())
                .usedAt(jpa.getUsedAt())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .createdBy(jpa.getCreatedBy())
                .updatedBy(jpa.getUpdatedBy())
                .build();
    }

    public VoucherUsageJpaEntity toJpa(VoucherUsage usage) {
        if (usage == null) return null;

        return VoucherUsageJpaEntity.builder()
                .id(usage.getId())
                .userId(usage.getUserId())
                .orderId(usage.getOrderId())
                .discountApplied(usage.getDiscountApplied())
                .usedAt(usage.getUsedAt())
                .createdAt(usage.getCreatedAt())
                .updatedAt(usage.getUpdatedAt())
                .createdBy(usage.getCreatedBy())
                .updatedBy(usage.getUpdatedBy())
                .build();
    }

    // ── UserVoucher ──

    public UserVoucher toDomain(UserVoucherJpaEntity jpa) {
        if (jpa == null) return null;

        return UserVoucher.builder()
                .id(jpa.getId())
                .userId(jpa.getUserId())
                .voucher(toDomain(jpa.getVoucher()))
                .collectedAt(jpa.getCollectedAt())
                .usedAt(jpa.getUsedAt())
                .used(jpa.isUsed())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .createdBy(jpa.getCreatedBy())
                .updatedBy(jpa.getUpdatedBy())
                .build();
    }

    public UserVoucherJpaEntity toJpa(UserVoucher domain) {
        if (domain == null) return null;

        return UserVoucherJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .voucher(toJpa(domain.getVoucher()))
                .collectedAt(domain.getCollectedAt())
                .usedAt(domain.getUsedAt())
                .used(domain.isUsed())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .build();
    }

    // ── Helpers ──

    private List<UUID> toList(Set<UUID> set) {
        return set != null ? List.copyOf(set) : List.of();
    }

    private VoucherScope parseScope(String rawScope) {
        if (rawScope == null || rawScope.isBlank()) {
            return VoucherScope.GLOBAL;
        }
        if ("ALL".equalsIgnoreCase(rawScope)) {
            // Backward-compatibility for legacy persisted value.
            return VoucherScope.GLOBAL;
        }
        return VoucherScope.valueOf(rawScope);
    }
}
