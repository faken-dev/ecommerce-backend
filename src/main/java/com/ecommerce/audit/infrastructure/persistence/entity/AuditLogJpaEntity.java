package com.ecommerce.audit.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogJpaEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant timestamp;

    private UUID userId;

    private String userName;

    @Column(nullable = false)
    private String action;

    private String resourceType;

    private String resourceId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String ipAddress;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
