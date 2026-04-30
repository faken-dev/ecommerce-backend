package com.ecommerce.voucher.infrastructure.persistence.repository;

import com.ecommerce.voucher.infrastructure.persistence.entity.UserVoucherJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVoucherJpaRepository extends JpaRepository<UserVoucherJpaEntity, UUID> {

    Optional<UserVoucherJpaEntity> findByUserIdAndVoucherId(UUID userId, UUID voucherId);

    Page<UserVoucherJpaEntity> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT uv FROM UserVoucherJpaEntity uv JOIN uv.voucher v " +
           "WHERE uv.userId = :userId AND uv.used = false " +
           "AND v.status = 'ACTIVE' " +
           "AND (v.validTo IS NULL OR v.validTo > CURRENT_TIMESTAMP)")
    Page<UserVoucherJpaEntity> findActiveVouchersByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndVoucherId(UUID userId, UUID voucherId);
}
