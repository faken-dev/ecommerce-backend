package com.ecommerce.voucher.application.usecase;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.voucher.domain.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteVoucherUseCase {

    private final VoucherRepository voucherRepository;

    @Transactional
    public void execute(UUID voucherId) {
        voucherRepository.findById(voucherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Voucher not found"));
        
        voucherRepository.deleteById(voucherId);
    }
}
