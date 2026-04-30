package com.ecommerce.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class RoleResponse {
    private UUID id;
    private String name;
    private String description;
    private Set<String> permissions;
    private Instant createdAt;

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Set<String> getPermissions() { return permissions; }
    public Instant getCreatedAt() { return createdAt; }
}
