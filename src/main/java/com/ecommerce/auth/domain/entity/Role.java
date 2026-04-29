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
    private Set<Permission> permissions;
    private Instant deletedAt;

    public static Role create(String name, String description) {
        return Role.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name(name)
                .description(description)
                .permissions(new HashSet<>())
                .deletedAt(null)
                .build();
    }

    public void addPermission(Permission permission) {
        if (this.permissions == null) this.permissions = new HashSet<>();
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        if (this.permissions != null) {
            this.permissions.remove(permission);
        }
    }

    public boolean hasPermission(String permissionName) {
        return permissions != null && permissions.stream().anyMatch(p -> p.getName().equals(permissionName));
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
