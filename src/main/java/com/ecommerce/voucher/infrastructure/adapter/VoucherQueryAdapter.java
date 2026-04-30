package com.ecommerce.voucher.infrastructure.adapter;

import com.ecommerce.order.application.port.VoucherQueryPort;
import com.ecommerce.voucher.application.command.ValidateVoucherCommand;
import com.ecommerce.voucher.application.dto.DiscountValidationResult;
import com.ecommerce.voucher.application.usecase.ValidateVoucherUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VoucherQueryAdapter implements VoucherQueryPort {

    private final ValidateVoucherUseCase validateVoucherUseCase;

    @Override
    public DiscountInfo calculateDiscount(String code, UUID userId, BigDecimal subtotal, BigDecimal shippingFee, Set<UUID> productIds, Set<UUID> categoryIds) {
        if (code == null || code.isBlank()) {
            return new DiscountInfo(false, BigDecimal.ZERO, "No voucher code provided");
        }

        try {
            DiscountValidationResult result = validateVoucherUseCase.execute(new ValidateVoucherCommand(
                    code,
                    userId,
                    subtotal,
                    shippingFee,
                    productIds,
                    categoryIds
            ));

            if (result.valid()) {
                return new DiscountInfo(true, result.discountAmount(), null);
            } else {
                return new DiscountInfo(false, BigDecimal.ZERO, result.message());
            }
        } catch (Exception e) {
            // Handle cases where voucher is not found or other errors
            return new DiscountInfo(false, BigDecimal.ZERO, e.getMessage());
        }
    }
}
