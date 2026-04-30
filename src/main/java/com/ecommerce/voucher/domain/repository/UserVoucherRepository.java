package com.ecommerce.voucher.domain.repository;

import com.ecommerce.voucher.domain.entity.UserVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserVoucherRepository {

    UserVoucher save(UserVoucher userVoucher);

    Optional<UserVoucher> findByUserIdAndVoucherId(UUID userId, UUID voucherId);

    Page<UserVoucher> findByUserId(UUID userId, Pageable pageable);

    Page<UserVoucher> findActiveVouchersByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndVoucherId(UUID userId, UUID voucherId);
}
