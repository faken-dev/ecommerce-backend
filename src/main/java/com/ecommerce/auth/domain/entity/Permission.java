package com.ecommerce.auth.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;

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

    /** Create a new permission. */
    public static Permission create(String name, String description) {
        return Permission.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name(name)
                .description(description)
                .deletedAt(null)
                .build();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
