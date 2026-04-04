package com.ecommerce.shared.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;


public abstract class AuditableEntity {

    private final UUID id;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // Constructor
    protected AuditableEntity() {
        this.id = UuidCreator.getTimeOrderedEpoch();
    }


    protected AuditableEntity(UUID id, Instant createdAt, Instant updatedAt, String createdBy, String updatedBy) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }


    // Domain logic
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