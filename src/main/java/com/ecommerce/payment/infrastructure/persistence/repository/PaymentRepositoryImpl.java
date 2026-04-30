package com.ecommerce.payment.infrastructure.persistence.repository;

import java.time.Instant;

import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.ecommerce.payment.infrastructure.persistence.mapper.PaymentDomainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentDomainMapper mapper;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        return jpaRepository.findById(payment.getId())
                .map(existing -> {
                    // Inline scalar field copy (replaces former mapper.copyScalarFieldsFrom)
                    existing.setOrderId(payment.getOrderId());
                    existing.setBuyerId(payment.getBuyerId());
                    existing.setAmount(payment.getAmount());
                    existing.setRefundedAmount(payment.getRefundedAmount());
                    existing.setCurrency(payment.getCurrency());
                    existing.setProvider(payment.getProvider().name());
                    existing.setMethodType(payment.getMethodType().name());
                    existing.setProviderReference(payment.getProviderReference());
                    existing.setStatus(payment.getStatus().name());
                    existing.setFailureReason(payment.getFailureReason());
                    existing.setFailureCode(payment.getFailureCode());
                    existing.setDescription(payment.getDescription());
                    existing.setReturnUrl(payment.getReturnUrl());
                    existing.setCancelUrl(payment.getCancelUrl());
                    existing.setPaidAt(payment.getPaidAt());
                    existing.setCancelledAt(payment.getCancelledAt());
                    existing.setExpiredAt(payment.getExpiredAt());
                    existing.setDeletedAt(payment.getDeletedAt());
                    existing.setIdempotencyKey(payment.getIdempotencyKey());
                    existing.setIpAddress(payment.getIpAddress());
                    existing.setUserAgent(payment.getUserAgent());
                    existing.setVersion(payment.getVersion());
                    // Sync refunds
                    existing.getRefunds().clear();
                    payment.getRefunds().forEach(refund -> {
                        if (refund.getDeletedAt() == null) {
                            existing.addRefund(mapper.toRefundJpa(refund, existing));
                        }
                    });
                    return mapper.toDomain(jpaRepository.save(existing));
                })
                .orElseGet(() -> {
                    PaymentJpaEntity jpa = mapper.toJpa(payment);
                    return mapper.toDomain(jpaRepository.save(jpa));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(UUID paymentId) {
        return jpaRepository.findByIdAndDeletedAtIsNull(paymentId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAllByOrderId(UUID orderId) {
        return jpaRepository.findAllByOrderIdAndDeletedAtIsNull(orderId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByProviderReference(String providerReference) {
        return jpaRepository.findByProviderReferenceAndDeletedAtIsNull(providerReference)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKeyAndDeletedAtIsNull(idempotencyKey)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.existsByIdempotencyKeyAndDeletedAtIsNull(idempotencyKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> findByBuyerId(UUID buyerId, Pageable pageable) {
        Page<PaymentJpaEntity> page = jpaRepository.findByBuyerId(buyerId, pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> findByBuyerIdAndStatus(UUID buyerId, PaymentStatus status, Pageable pageable) {
        Page<PaymentJpaEntity> page = jpaRepository.findByBuyerIdAndStatus(buyerId, status.name(), pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> findAll(Pageable pageable) {
        Page<PaymentJpaEntity> page = jpaRepository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> findByStatus(PaymentStatus status, Pageable pageable) {
        Page<PaymentJpaEntity> page = jpaRepository.findByStatus(status.name(), pageable);
        return new PageImpl<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> findByOrderId(UUID orderId, Pageable pageable) {
        List<PaymentJpaEntity> all = jpaRepository.findAllByOrderIdAndDeletedAtIsNull(orderId);
        return new PageImpl<>(all.stream().map(mapper::toDomain).toList(), pageable, all.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findStuckPayments(Instant createdBefore, Pageable pageable) {
        return jpaRepository.findStuckPayments(createdBefore, pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID paymentId) {
        jpaRepository.findById(paymentId).ifPresent(entity -> {
            entity.setDeletedAt(Instant.now());
            jpaRepository.save(entity);
        });
    }
}
