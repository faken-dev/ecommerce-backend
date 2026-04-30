package com.ecommerce.payment.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.VersionedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payment_payments")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentJpaEntity extends VersionedJpaEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;


    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "refunded_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(name = "method_type", nullable = false, length = 20)
    private String methodType;

    @Column(name = "provider_reference", length = 255)
    private String providerReference;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(length = 500)
    private String description;

    @Column(name = "return_url", length = 1000)
    private String returnUrl;

    @Column(name = "cancel_url", length = 1000)
    private String cancelUrl;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @Column(name = "idempotency_key", unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RefundJpaEntity> refunds = new ArrayList<>();

    public void addRefund(RefundJpaEntity refund) {
        refunds.add(refund);
        refund.setPayment(this);
    }
}
