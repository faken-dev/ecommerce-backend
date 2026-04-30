package com.ecommerce.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class UserAdminResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private boolean active;
    private boolean emailVerified;
    private Set<String> roles;
    private String profilePictureUrl;
    private Instant createdAt;

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public boolean isActive() { return active; }
    public boolean isEmailVerified() { return emailVerified; }
    public Set<String> getRoles() { return roles; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public Instant getCreatedAt() { return createdAt; }
}
