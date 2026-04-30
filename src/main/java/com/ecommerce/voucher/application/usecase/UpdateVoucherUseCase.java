package com.ecommerce.voucher.application.usecase;

import java.time.Instant;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.voucher.application.dto.VoucherResponse;
import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.event.VoucherStatusChangedEvent;
import com.ecommerce.voucher.domain.repository.VoucherRepository;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import com.ecommerce.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Updates voucher status (activate/disable/expire).
 * Only status transitions are allowed after creation.
 */
@Service
@RequiredArgsConstructor
public class UpdateVoucherUseCase {

    private final VoucherRepository voucherRepository;
    private final EventPublisher eventPublisher;

    /** Activates a pending or disabled voucher. */
    @Transactional
    public VoucherResponse activate(UUID voucherId, UUID changedBy) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND));

        VoucherStatus previous = voucher.getStatus();
        voucher.activate();
        Voucher saved = voucherRepository.save(voucher);

        eventPublisher.publish(new VoucherStatusChangedEvent(
                saved.getId(), saved.getCode(), previous, saved.getStatus(), Instant.now()));

        return VoucherResponse.from(saved);
    }

    /** Disables an active voucher (admin/seller action). */
    @Transactional
    public VoucherResponse disable(UUID voucherId, UUID changedBy) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND));

        VoucherStatus previous = voucher.getStatus();
        voucher.disable();
        Voucher saved = voucherRepository.save(voucher);

        eventPublisher.publish(new VoucherStatusChangedEvent(
                saved.getId(), saved.getCode(), previous, saved.getStatus(), Instant.now()));

        return VoucherResponse.from(saved);
    }

    /** Marks a voucher as expired. */
    @Transactional
    public VoucherResponse expire(UUID voucherId, UUID changedBy) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND));

        VoucherStatus previous = voucher.getStatus();
        voucher.markAsExpired();
        Voucher saved = voucherRepository.save(voucher);

        eventPublisher.publish(new VoucherStatusChangedEvent(
                saved.getId(), saved.getCode(), previous, saved.getStatus(), Instant.now()));

        return VoucherResponse.from(saved);
    }
}
