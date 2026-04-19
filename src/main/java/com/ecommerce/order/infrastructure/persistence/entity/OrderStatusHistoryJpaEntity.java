package com.ecommerce.order.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "orders_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderStatusHistoryJpaEntity extends AuditableJpaEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 20)
    private String toStatus;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "changed_by_role", length = 20)
    private String changedByRole;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "jsonb")
    private String metadata;
}
