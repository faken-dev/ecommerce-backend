package com.ecommerce.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base JPA entity for all audited tables.
 *
 * <p>The {@code createdBy} / {@code updatedBy} fields are {@code UUID} (type-safe).
 * The JDBC conversion ({@code VARCHAR} ↔ {@code UUID}) is handled by
 * {@link #setCreatedBy(UUID)} / {@link #setUpdatedBy(UUID)} so the domain and
 * mappers never deal with strings.
 *
 * <p>UUID resolution: see {@link com.ecommerce.shared.config.JpaConfig#auditorProvider()}.
 */
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Stores the actor's UUID. JDBC auto-converts VARCHAR → UUID via
     * {@link #setCreatedBy(UUID)} so callers always use the UUID type.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    /**
     * Stores the actor's UUID. JDBC auto-converts VARCHAR → UUID via
     * {@link #setUpdatedBy(UUID)} so callers always use the UUID type.
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    // ── JDBC String → UUID conversion ─────────────────────────────────────────

    /**
     * Called by JPA when reading the {@code created_by} column.
     * Allows {@code createdBy} to be {@code UUID} in the entity while
     * the database column remains {@code VARCHAR}.
     */
    @Transient
    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    @Transient
    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}
