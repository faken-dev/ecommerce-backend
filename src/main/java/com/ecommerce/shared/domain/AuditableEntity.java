package com.ecommerce.shared.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all domain entities that need audit fields.
 */
@Getter
@NoArgsConstructor
@SuperBuilder
public abstract class AuditableEntity {

    @Builder.Default
    private UUID id = UuidCreator.getTimeOrderedEpoch();

    public void setId(UUID id) { this.id = id; }
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }

    /**
     * Updates the updatedAt timestamp.
     * Protected so only sub-entities can trigger it.
     */
    protected void touchUpdate() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditableEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
