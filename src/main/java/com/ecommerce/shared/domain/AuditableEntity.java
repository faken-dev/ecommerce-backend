package com.ecommerce.shared.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class AuditableEntity {
    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

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