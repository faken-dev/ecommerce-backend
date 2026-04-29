package com.ecommerce.auth.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Role extends AuditableEntity {
    private String name;
    private String description;

    @SuperBuilder.Default
    private Set<Permission> permissions = new HashSet<>();

    private Instant deletedAt;

    public static Role create(String name, String description) {
        Instant now = Instant.now();
        return Role.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name(name)
                .description(description)
                .permissions(new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
