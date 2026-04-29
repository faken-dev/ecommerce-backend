package com.ecommerce.auth.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Permission extends AuditableEntity {
    private String name;
    private String description;

    /** Soft-delete timestamp. Null = active record. */
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
