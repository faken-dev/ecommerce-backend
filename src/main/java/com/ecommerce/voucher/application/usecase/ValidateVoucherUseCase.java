package com.ecommerce.voucher.application.usecase;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.voucher.application.command.ValidateVoucherCommand;
import com.ecommerce.voucher.application.dto.DiscountValidationResult;
import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.repository.VoucherRepository;
import com.ecommerce.voucher.domain.repository.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * Validates (previews) a voucher without applying it.
 * Used to show discount preview in cart/checkout page.
 */
@Service
@RequiredArgsConstructor
public class ValidateVoucherUseCase {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Transactional(readOnly = true)
    public DiscountValidationResult execute(ValidateVoucherCommand cmd) {

        // ── Find voucher ──
        Voucher voucher = voucherRepository.findByCode(cmd.code().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND));

        int userUsage = 0;
        if (cmd.userId() != null) {
            userUsage = voucherRepository.countUsageByUserId(voucher.getId(), cmd.userId());
            
            // ── Collection check ──
            if (voucher.isRequiresCollection() && 
                !userVoucherRepository.existsByUserIdAndVoucherId(cmd.userId(), voucher.getId())) {
                return DiscountValidationResult.notApplicable("Bạn cần thu thập voucher này trước khi sử dụng");
            }
        } else if (voucher.isRequiresCollection()) {
            // Anonymous users can't use vouchers that require collection
            return DiscountValidationResult.notApplicable("Vui lòng đăng nhập và thu thập voucher này");
        }

        // ── Calculate discount ──
        Set<UUID> productIds = cmd.productIds() != null ? cmd.productIds() : Set.of();
        Set<UUID> categoryIds = cmd.categoryIds() != null ? cmd.categoryIds() : Set.of();

        Voucher.DiscountResult result = voucher.calculateDiscount(
                cmd.subtotal() != null ? cmd.subtotal() : BigDecimal.ZERO,
                cmd.shippingFee(),
                productIds,
                categoryIds,
                userUsage
        );

        if (!result.isApplicable()) {
            return DiscountValidationResult.notApplicable(result.getReason());
        }

        return DiscountValidationResult.applicable(
                voucher,
                result.getDiscountAmount(),
                result.getDescription()
        );
    }
}
