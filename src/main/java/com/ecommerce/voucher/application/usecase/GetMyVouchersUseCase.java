package com.ecommerce.voucher.application.usecase;

import com.ecommerce.voucher.application.dto.VoucherResponse;
import com.ecommerce.voucher.domain.entity.UserVoucher;
import com.ecommerce.voucher.domain.repository.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMyVouchersUseCase {

    private final UserVoucherRepository userVoucherRepository;

    @Transactional(readOnly = true)
    public Page<VoucherResponse> execute(UUID userId, boolean activeOnly, Pageable pageable) {
        Page<UserVoucher> userVouchers = activeOnly
                ? userVoucherRepository.findActiveVouchersByUserId(userId, pageable)
                : userVoucherRepository.findByUserId(userId, pageable);
                
        return userVouchers.map(uv -> VoucherResponse.from(uv.getVoucher()));
    }
}
