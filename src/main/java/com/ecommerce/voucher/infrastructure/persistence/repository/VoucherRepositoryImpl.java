package com.ecommerce.voucher.infrastructure.persistence.repository;

import java.math.BigDecimal;

import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import com.ecommerce.voucher.domain.repository.VoucherRepository;
import com.ecommerce.voucher.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ecommerce.voucher.infrastructure.persistence.entity.VoucherUsageJpaEntity;
import com.ecommerce.voucher.infrastructure.persistence.mapper.VoucherDomainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class VoucherRepositoryImpl implements VoucherRepository {

    private final VoucherJpaRepository jpaRepository;
    private final VoucherUsageJpaRepository usageJpaRepository;
    private final VoucherDomainMapper mapper;

    @Override
    public Voucher save(Voucher voucher) {
        return jpaRepository.findById(voucher.getId())
                .map(existing -> {
                    mapper.copyScalarFieldsFrom(voucher, existing);
                    return mapper.toDomain(jpaRepository.save(existing));
                })
                .orElseGet(() -> {
                    VoucherJpaEntity jpa = mapper.toJpa(voucher);
                    return mapper.toDomain(jpaRepository.save(jpa));
                });
    }
    @Override
    @Transactional(readOnly = true)
    public Page<Voucher> findAll(Pageable pageable) {
        Page<VoucherJpaEntity> page = jpaRepository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voucher> findById(UUID voucherId) {
        return jpaRepository.findById(voucherId)
                .filter(e -> e.getDeletedAt() == null)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Voucher> findByIdWithLock(UUID voucherId) {
        return jpaRepository.findByIdWithLock(voucherId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voucher> findByCode(String code) {
        return jpaRepository.findByCodeIgnoreCaseAndDeletedAtIsNull(code)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Voucher> findByCodeWithLock(String code) {
        return jpaRepository.findByCodeWithLock(code)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCodeIgnoreCase(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Voucher> findBySellerIdAndStatus(UUID sellerId, VoucherStatus status, Pageable pageable) {
        Page<VoucherJpaEntity> page = jpaRepository.findBySellerIdAndStatus(sellerId, status.name(), pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Voucher> findByStatus(VoucherStatus status, Pageable pageable) {
        Page<VoucherJpaEntity> page = jpaRepository.findByStatus(status.name(), pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Voucher> findActiveVouchers(Instant now, Pageable pageable) {
        Page<VoucherJpaEntity> page = jpaRepository.findActiveVouchers(now, pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Voucher> findExpiredBefore(Instant before, Pageable pageable) {
        Page<VoucherJpaEntity> page = jpaRepository.findExpiredBefore(before, pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Voucher> findBySellerId(UUID sellerId, Pageable pageable) {
        Page<VoucherJpaEntity> page = jpaRepository.findBySellerId(sellerId, pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    public void delete(Voucher voucher) {
        voucher.softDelete();
        save(voucher);
    }

    @Override
    public void deleteById(UUID id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public int countUsageByUserId(UUID voucherId, UUID userId) {
        return usageJpaRepository.countByVoucherIdAndUserId(voucherId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countUsageByOrderId(UUID orderId) {
        return usageJpaRepository.countByOrderId(orderId);
    }

    @Override
    public void recordUsage(Voucher voucher, UUID userId, UUID orderId, BigDecimal discountAmount) {
        VoucherJpaEntity voucherJpa = jpaRepository.findById(voucher.getId())
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found in persistence"));

        VoucherUsageJpaEntity usage = 
            VoucherUsageJpaEntity.builder()
                .id(UUID.randomUUID())
                .voucher(voucherJpa)
                .userId(userId)
                .orderId(orderId)
                .discountApplied(discountAmount)
                .usedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        usageJpaRepository.save(usage);
    }
}
