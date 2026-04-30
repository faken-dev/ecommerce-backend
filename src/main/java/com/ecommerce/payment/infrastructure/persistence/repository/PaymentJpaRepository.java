package com.ecommerce.payment.infrastructure.persistence.repository;

import java.time.Instant;

import java.util.List;

import com.ecommerce.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<PaymentJpaEntity> findAllByOrderIdAndDeletedAtIsNull(UUID orderId);

    Optional<PaymentJpaEntity> findFirstByOrderIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID orderId);

    Optional<PaymentJpaEntity> findByProviderReferenceAndDeletedAtIsNull(String providerReference);

    Optional<PaymentJpaEntity> findByIdempotencyKeyAndDeletedAtIsNull(String idempotencyKey);

    boolean existsByIdempotencyKeyAndDeletedAtIsNull(String idempotencyKey);

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.buyerId = :buyerId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<PaymentJpaEntity> findByBuyerId(@Param("buyerId") UUID buyerId, Pageable pageable);

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.buyerId = :buyerId AND p.status = :status AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<PaymentJpaEntity> findByBuyerIdAndStatus(@Param("buyerId") UUID buyerId, @Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.status = :status AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<PaymentJpaEntity> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.status IN ('PENDING', 'PROCESSING', 'REFUNDING') " +
           "AND p.createdAt < :createdBefore AND p.deletedAt IS NULL")
    List<PaymentJpaEntity> findStuckPayments(@Param("createdBefore") Instant createdBefore, Pageable pageable);
}
