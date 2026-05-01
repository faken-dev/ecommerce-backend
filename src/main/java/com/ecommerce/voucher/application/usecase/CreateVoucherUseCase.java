package com.ecommerce.voucher.application.usecase;

import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.voucher.application.command.CreateVoucherCommand;
import com.ecommerce.voucher.application.dto.VoucherResponse;
import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import com.ecommerce.voucher.domain.event.VoucherCreatedEvent;
import com.ecommerce.voucher.domain.repository.VoucherRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

/**
 * Creates a new voucher.
 * Seller creates their own voucher; admin creates platform-wide voucher.
 */
@Service
@RequiredArgsConstructor
public class CreateVoucherUseCase {

    private final VoucherRepository voucherRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public VoucherResponse execute(CreateVoucherCommand cmd, UUID createdBy) {
        String normalizedCode = cmd.code().trim().toUpperCase();

        // - Uniqueness check -----
        if (voucherRepository.existsByCode(normalizedCode)) {
            throw new BusinessException(ErrorCode.VOUCHER_CODE_ALREADY_EXISTS);
        }

        // - Build applicable scope sets-
        var productIds = cmd.applicableProductIds() != null
                ? new HashSet<>(cmd.applicableProductIds())
                : new HashSet<UUID>();
        var categoryIds = cmd.applicableCategoryIds() != null
                ? new HashSet<>(cmd.applicableCategoryIds())
                : new HashSet<UUID>();

        // - Domain Model ------
        Instant now = Instant.now();

        Voucher voucher = Voucher.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .code(normalizedCode)
                .name(cmd.name().trim())
                .type(cmd.type())
                .scope(cmd.scope())
                .status(VoucherStatus.ACTIVE)
                .discountValue(cmd.discountValue())
                .minOrderAmount(cmd.minOrderAmount() != null ? cmd.minOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(cmd.maxDiscountAmount())
                .maxUsageTotal(cmd.maxUsageTotal())
                .maxUsagePerUser(cmd.maxUsagePerUser())
                .validFrom(cmd.validFrom())
                .validTo(cmd.validTo())
                .sellerId(cmd.sellerId())
                .applicableProductIds(productIds)
                .applicableCategoryIds(categoryIds)
                .requiresCollection(cmd.requiresCollection() != null ? cmd.requiresCollection() : false)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .currentUsageCount(0)
                .build();

        Voucher saved = voucherRepository.save(voucher);

        eventPublisher.publish(new VoucherCreatedEvent(
                saved.getId(),
                saved.getCode(),
                saved.getName(),
                saved.getType(),
                saved.getScope(),
                saved.getDiscountValue(),
                saved.getMaxDiscountAmount(),
                saved.getMinOrderAmount(),
                saved.getMaxUsageTotal(),
                saved.getMaxUsagePerUser(),
                saved.getValidFrom(),
                saved.getValidTo(),
                saved.getSellerId(),
                now
        ));

        return VoucherResponse.from(saved);
    }
}
