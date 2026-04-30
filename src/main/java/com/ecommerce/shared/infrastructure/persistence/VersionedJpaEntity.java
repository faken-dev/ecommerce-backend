package com.ecommerce.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base JPA entity for tables that require optimistic locking.
 * Extends {@link AuditableJpaEntity} with a {@code version} column.
 */
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
public abstract class VersionedJpaEntity extends AuditableJpaEntity {

    /**
     * Optimistic locking version. Automatically incremented by JPA on every UPDATE.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
