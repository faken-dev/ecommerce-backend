package com.ecommerce.shared.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

/**
 * Base class for all domain entities that need audit fields.
 *
 * <p>The {@code createdBy} / {@code updatedBy} fields store the actor's UUID,
 * matching the JPA entity type for consistency across layers.
 */
public abstract class AuditableEntity {

    private final UUID id;

    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // ── Constructors ────────────────────────────────────────────────────────────

    /** Default constructor — generates a time-ordered UUID as id. */
    protected AuditableEntity() {
        this.id = UuidCreator.getTimeOrderedEpoch();
    }

    /**
     * Full constructor for reconstitution from persistence.
     * All parameters are non-null (IDs must be present in stored entities).
     */
    protected AuditableEntity(UUID id, Instant createdAt, Instant updatedAt,
                             UUID createdBy, UUID updatedBy) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public UUID getUpdatedBy() { return updatedBy; }

    public void setUpdateAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public void setUpdatedBy(UUID updatedBy)    { this.updatedBy = updatedBy; }
    public void setCreatedAt(Instant createdAt)  { this.createdAt = createdAt; }
    public void setCreatedBy(UUID createdBy)    { this.createdBy = createdBy; }

    // ── Domain logic ────────────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof AuditableEntity that
                && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}