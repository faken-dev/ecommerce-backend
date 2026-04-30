package com.ecommerce.payment.domain.repository;

import java.time.Instant;

import java.util.List;

import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID paymentId);

    List<Payment> findAllByOrderId(UUID orderId);

    Optional<Payment> findByProviderReference(String providerReference);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<Payment> findByBuyerId(UUID buyerId, Pageable pageable);

    Page<Payment> findByBuyerIdAndStatus(UUID buyerId, PaymentStatus status, Pageable pageable);

    Page<Payment> findAll(Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findByOrderId(UUID orderId, Pageable pageable);

    List<Payment> findStuckPayments(Instant createdBefore, Pageable pageable);

    void deleteById(UUID paymentId);
}
