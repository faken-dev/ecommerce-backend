package com.ecommerce.auth.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Role extends AuditableEntity {
    private final String name;
    private final String description;
    private final Set<Permission> permissions;

    /** Soft-delete timestamp. Null = active record. */
    private Instant deletedAt;

    /** Create a new role. */
    public static Role create(String name, String description) {
        return Role.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name(name)
                .description(description)
                .permissions(new HashSet<>())
                .deletedAt(null)
                .build();
    }

    @Builder
    public Role(UUID id, String name, String description, Set<Permission> permissions,
                Instant createdAt, Instant updatedAt,
                UUID createdBy, UUID updatedBy,
                Instant deletedAt) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.name = name;
        this.description = description;
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.deletedAt = deletedAt;
    }

    // ── Domain Rules ──────────────────────────────────────────────────────────
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream().anyMatch(p -> p.getName().equals(permissionName));
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
