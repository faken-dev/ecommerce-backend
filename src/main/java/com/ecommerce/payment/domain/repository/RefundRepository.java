package com.ecommerce.payment.domain.repository;

import com.ecommerce.payment.domain.entity.Refund;
import com.ecommerce.payment.domain.entity.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface RefundRepository {

    void saveRefund(UUID paymentId, Refund refund);

    Optional<Refund> findById(UUID refundId);

    Page<Refund> findByPaymentId(UUID paymentId, Pageable pageable);

    Page<Refund> findByOrderId(UUID orderId, Pageable pageable);

    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);

    Page<Refund> findPendingForReview(Pageable pageable);

    Page<Refund> findByRequestedBy(UUID requestedBy, Pageable pageable);
}
