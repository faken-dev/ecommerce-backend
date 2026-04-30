package com.ecommerce.voucher.domain.repository;

import java.util.UUID;

/**
 * Repository interface for VoucherUsage entity.
 * Lives in the domain layer - implemented in infrastructure.
 */
public interface VoucherUsageRepository {

    /** Count how many times a specific user has used a voucher. */
    int countByVoucherIdAndUserId(UUID voucherId, UUID userId);
}
