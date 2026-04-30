package com.ecommerce.voucher.domain.repository;

import java.math.BigDecimal;

import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for the Voucher aggregate root.
 * Lives in the domain layer (ports) - implemented in infrastructure.
 */
public interface VoucherRepository {

    /** Persist or update a voucher. */
    Voucher save(Voucher voucher);

    /** Find all vouchers (admin view). */
    Page<Voucher> findAll(Pageable pageable);

    /** Find by ID (excluding soft-deleted). */
    Optional<Voucher> findById(UUID voucherId);

    /** Find by unique code (case-insensitive). */
    Optional<Voucher> findByCode(String code);

    /** Find by unique code with pessimistic lock. */
    Optional<Voucher> findByCodeWithLock(String code);

    /** Find by ID with pessimistic lock. */
    Optional<Voucher> findByIdWithLock(UUID voucherId);

    /** Check if a code already exists (for uniqueness validation). */
    boolean existsByCode(String code);

    /** Find all active vouchers for a seller (including platform-wide null sellerId). */
    Page<Voucher> findBySellerIdAndStatus(UUID sellerId, VoucherStatus status, Pageable pageable);

    /** Find all vouchers by status. */
    Page<Voucher> findByStatus(VoucherStatus status, Pageable pageable);

    /** Find all active vouchers (usable). */
    Page<Voucher> findActiveVouchers(Instant now, Pageable pageable);

    /** Find all vouchers expiring before a given time (for scheduler). */
    Page<Voucher> findExpiredBefore(Instant before, Pageable pageable);

    /** Find vouchers for seller. */
    Page<Voucher> findBySellerId(UUID sellerId, Pageable pageable);

    /** Delete (soft-delete) a voucher. */
    void delete(Voucher voucher);

    /** Delete (soft-delete) a voucher by ID. */
    void deleteById(UUID id);

    /** Count how many times a user has used a specific voucher. */
    int countUsageByUserId(UUID voucherId, UUID userId);

    /** Count usage by order ID (for idempotency). */
    int countUsageByOrderId(UUID orderId);

    /** Record a new usage of a voucher. */
    void recordUsage(Voucher voucher, UUID userId, UUID orderId, BigDecimal discountAmount);
}
