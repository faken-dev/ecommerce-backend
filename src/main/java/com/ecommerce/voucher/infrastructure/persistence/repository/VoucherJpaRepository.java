package com.ecommerce.voucher.infrastructure.persistence.repository;

import com.ecommerce.voucher.infrastructure.persistence.entity.VoucherJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoucherJpaRepository extends JpaRepository<VoucherJpaEntity, UUID> {

    Optional<VoucherJpaEntity> findByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VoucherJpaEntity v WHERE LOWER(v.code) = LOWER(:code) AND v.deletedAt IS NULL")
    Optional<VoucherJpaEntity> findByCodeWithLock(@Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VoucherJpaEntity v WHERE v.id = :id AND v.deletedAt IS NULL")
    Optional<VoucherJpaEntity> findByIdWithLock(@Param("id") UUID id);

    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("SELECT v FROM VoucherJpaEntity v WHERE v.sellerId = :sellerId AND v.status = :status AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<VoucherJpaEntity> findBySellerIdAndStatus(
            @Param("sellerId") UUID sellerId,
            @Param("status") String status,
            Pageable pageable);

    @Query("SELECT v FROM VoucherJpaEntity v WHERE v.status = :status AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<VoucherJpaEntity> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT v FROM VoucherJpaEntity v WHERE v.status = 'ACTIVE' AND v.deletedAt IS NULL " +
           "AND (v.validFrom IS NULL OR v.validFrom <= :now) " +
           "AND (v.validTo IS NULL OR v.validTo >= :now) " +
           "ORDER BY v.createdAt DESC")
    Page<VoucherJpaEntity> findActiveVouchers(@Param("now") Instant now, Pageable pageable);

    @Query("SELECT v FROM VoucherJpaEntity v WHERE v.validTo IS NOT NULL AND v.validTo < :before AND v.status NOT IN ('EXPIRED','DEPLETED') AND v.deletedAt IS NULL")
    Page<VoucherJpaEntity> findExpiredBefore(@Param("before") Instant before, Pageable pageable);

    @Query("SELECT v FROM VoucherJpaEntity v WHERE v.sellerId = :sellerId AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<VoucherJpaEntity> findBySellerId(@Param("sellerId") UUID sellerId, Pageable pageable);
}
