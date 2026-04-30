package com.ecommerce.voucher.application.usecase;

import java.time.Instant;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.voucher.application.dto.VoucherResponse;
import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import com.ecommerce.voucher.domain.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Retrieves vouchers - supports buyer browsing and seller management.
 */
@Service
@RequiredArgsConstructor
public class GetVoucherUseCase {

    private final VoucherRepository voucherRepository;

    @Transactional(readOnly = true)
    public VoucherResponse execute(UUID voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND));
        return VoucherResponse.from(voucher);
    }

    @Transactional(readOnly = true)
    public VoucherResponse executeByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND));
        return VoucherResponse.from(voucher);
    }

    /**
     * List all active public vouchers (for buyer browsing).
     */
    @Transactional(readOnly = true)
    public Page<VoucherResponse> listActiveVouchers(Pageable pageable) {
        return voucherRepository
                .findActiveVouchers(Instant.now(), pageable)
                .map(VoucherResponse::from);
    }

    /**
     * List vouchers for seller dashboard.
     */
    @Transactional(readOnly = true)
    public Page<VoucherResponse> listBySeller(UUID sellerId, Pageable pageable) {
        return voucherRepository.findBySellerId(sellerId, pageable)
                .map(VoucherResponse::from);
    }

    /**
     * List vouchers for seller, filtered by status.
     */
    @Transactional(readOnly = true)
    public Page<VoucherResponse> listBySellerAndStatus(
            UUID sellerId, VoucherStatus status, Pageable pageable) {
        return voucherRepository.findBySellerIdAndStatus(sellerId, status, pageable)
                .map(VoucherResponse::from);
    }

    /**
     * Admin: list all vouchers.
     */
    @Transactional(readOnly = true)
    public Page<VoucherResponse> listAll(VoucherStatus status, Pageable pageable) {
        if (status != null) {
            return voucherRepository.findByStatus(status, pageable)
                    .map(VoucherResponse::from);
        }
        return voucherRepository.findAll(pageable)
                .map(VoucherResponse::from);
    }
}
