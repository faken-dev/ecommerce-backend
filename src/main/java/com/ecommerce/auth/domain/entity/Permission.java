package com.ecommerce.auth.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Permission extends AuditableEntity {
    private final String name;
    private final String description;

    /** Soft-delete timestamp. Null = active record. */
    private Instant deletedAt;

    /** Create a new permission.  */
    public static Permission create(String name, String description) {
        return Permission.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name(name)
                .description(description)
                .deletedAt(null)
                .build();
    }

    @Builder
    public Permission(UUID id, String name, String description,
                      Instant createdAt, Instant updatedAt,
                      UUID createdBy, UUID updatedBy,
                      Instant deletedAt) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.name = name;
        this.description = description;
        this.deletedAt = deletedAt;
    }

    // ── Domain Rules ──────────────────────────────────────────────────────────

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
