package com.ecommerce.payment.infrastructure.persistence.repository;

import com.ecommerce.payment.infrastructure.persistence.entity.RefundJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, UUID> {

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.payment.id = :paymentId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<RefundJpaEntity> findByPaymentId(@Param("paymentId") UUID paymentId, Pageable pageable);

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.orderId = :orderId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<RefundJpaEntity> findByOrderId(@Param("orderId") UUID orderId, Pageable pageable);

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.status = :status AND r.deletedAt IS NULL ORDER BY r.createdAt ASC")
    Page<RefundJpaEntity> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.status = 'PENDING' AND r.deletedAt IS NULL ORDER BY r.createdAt ASC")
    Page<RefundJpaEntity> findPendingForReview(Pageable pageable);

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.requestedBy = :requestedBy AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<RefundJpaEntity> findByRequestedBy(@Param("requestedBy") UUID requestedBy, Pageable pageable);
}
