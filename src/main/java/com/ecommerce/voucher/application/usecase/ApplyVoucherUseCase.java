package com.ecommerce.voucher.application.usecase;

import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.voucher.application.command.ApplyVoucherCommand;
import com.ecommerce.voucher.application.dto.DiscountValidationResult;
import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.event.VoucherAppliedEvent;
import com.ecommerce.voucher.domain.repository.VoucherRepository;
import com.ecommerce.voucher.domain.repository.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Applies (commits) a voucher to an order after payment is confirmed.
 * Records usage and increments the voucher's usage counter.
 */
@Service
@RequiredArgsConstructor
public class ApplyVoucherUseCase {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public DiscountValidationResult execute(ApplyVoucherCommand cmd) {

        // ── Find voucher with Lock ──
        Voucher voucher = voucherRepository.findByCodeWithLock(cmd.code().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND));

        // ── Collection check ──
        if (voucher.isRequiresCollection() && 
            !userVoucherRepository.existsByUserIdAndVoucherId(cmd.userId(), voucher.getId())) {
            throw new BusinessException(ErrorCode.VOUCHER_NOT_APPLICABLE, "Bạn cần thu thập voucher này trước khi sử dụng");
        }

        // ── Calculate discount ──
        Set<UUID> productIds = cmd.productIds() != null ? cmd.productIds() : Set.of();
        Set<UUID> categoryIds = cmd.categoryIds() != null ? cmd.categoryIds() : Set.of();
        int userUsage = voucherRepository.countUsageByUserId(voucher.getId(), cmd.userId());

        Voucher.DiscountResult result = voucher.calculateDiscount(
                cmd.subtotal() != null ? cmd.subtotal() : BigDecimal.ZERO,
                cmd.shippingFee(),
                productIds,
                categoryIds,
                userUsage
        );

        if (!result.isApplicable()) {
            throw new BusinessException(ErrorCode.VOUCHER_NOT_APPLICABLE, result.getReason());
        }

        // ── Idempotency Check ── Check if this order already has recorded usage ──
        if (voucherRepository.countUsageByOrderId(cmd.orderId()) > 0) {
            return DiscountValidationResult.applicable(
                    voucher,
                    result.getDiscountAmount(),
                    "Voucher already applied to this order"
            );
        }

        // ── Persist usage record ──
        voucherRepository.recordUsage(voucher, cmd.userId(), cmd.orderId(), result.getDiscountAmount());

        // ── Increment usage count on voucher ──
        voucher.incrementUsage();
        voucherRepository.save(voucher);

        // ── Update UserVoucher (if collected) ──
        userVoucherRepository.findByUserIdAndVoucherId(cmd.userId(), voucher.getId())
                .ifPresent(uv -> {
                    uv.markAsUsed();
                    userVoucherRepository.save(uv);
                });

        // ── Publish event for Order module to apply discount ──
        eventPublisher.publish(new VoucherAppliedEvent(
                voucher.getId(),
                voucher.getCode(),
                cmd.orderId(),
                cmd.userId(),
                voucher.getType(),
                result.getDiscountAmount(),
                Instant.now()
        ));

        return DiscountValidationResult.applicable(
                voucher,
                result.getDiscountAmount(),
                result.getDescription()
        );
    }
}
