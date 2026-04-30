package com.ecommerce.voucher.application.usecase;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.voucher.application.dto.VoucherResponse;
import com.ecommerce.voucher.domain.entity.UserVoucher;
import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import com.ecommerce.voucher.domain.repository.UserVoucherRepository;
import com.ecommerce.voucher.domain.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectVoucherUseCase {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Transactional
    public VoucherResponse execute(String code, UUID userId) {
        Voucher voucher = voucherRepository.findByCode(code.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND));

        // 1. Check if already collected
        if (userVoucherRepository.existsByUserIdAndVoucherId(userId, voucher.getId())) {
            throw new BusinessException(ErrorCode.VOUCHER_ALREADY_COLLECTED);
        }

        // 2. Check if voucher is active and not expired
        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.VOUCHER_INVALID);
        }
        if (voucher.getValidTo() != null && voucher.getValidTo().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.VOUCHER_EXPIRED);
        }

        // 3. Check if depleted
        if (voucher.getMaxUsageTotal() > 0 && voucher.getCurrentUsageCount() >= voucher.getMaxUsageTotal()) {
            throw new BusinessException(ErrorCode.VOUCHER_DEPLETED);
        }

        // 4. Create UserVoucher record
        UserVoucher userVoucher = UserVoucher.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .userId(userId)
                .voucher(voucher)
                .collectedAt(Instant.now())
                .used(false)
                .build();

        userVoucherRepository.save(userVoucher);

        return VoucherResponse.from(voucher);
    }
}
