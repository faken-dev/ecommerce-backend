package com.ecommerce.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class UserDetailAdminResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private boolean active;
    private boolean emailVerified;
    private Set<String> roles;
    private Instant createdAt;
    
    // Profile details
    private String profilePictureUrl;
    private String bio;
    private LocalDate dateOfBirth;
    private String gender;
    
    // Addresses
    private List<AddressResponse> addresses;

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public boolean isActive() { return active; }
    public boolean isEmailVerified() { return emailVerified; }
    public Set<String> getRoles() { return roles; }
    public Instant getCreatedAt() { return createdAt; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public String getBio() { return bio; }
    public String getGender() { return gender; }
    public List<AddressResponse> getAddresses() { return addresses; }
}
