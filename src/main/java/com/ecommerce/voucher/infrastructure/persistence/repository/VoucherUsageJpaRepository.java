package com.ecommerce.voucher.infrastructure.persistence.repository;

import com.ecommerce.voucher.infrastructure.persistence.entity.VoucherUsageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VoucherUsageJpaRepository extends JpaRepository<VoucherUsageJpaEntity, UUID> {

    @Query("SELECT COUNT(vu) FROM VoucherUsageJpaEntity vu WHERE vu.voucher.id = :voucherId AND vu.userId = :userId")
    int countByVoucherIdAndUserId(@Param("voucherId") UUID voucherId, @Param("userId") UUID userId);

    int countByOrderId(UUID orderId);
}
