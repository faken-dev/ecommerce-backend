package com.ecommerce.user.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserProfile extends AuditableEntity {
    public enum Gender { MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY }

    private UUID userId;
    private String fullName;
    private String bio;
    private String profilePictureUrl;
    private LocalDate dateOfBirth;
    private Gender gender;
    private UUID defaultAddressId;

    public static UserProfile create(UUID userId, String fullName) {
        Instant now = Instant.now();
        return UserProfile.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .userId(userId)
                .fullName(fullName)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static UserProfile createFromRegistration(UUID userId, String fullName) {
        Instant now = Instant.now();
        return UserProfile.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .userId(userId)
                .fullName(fullName)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateProfile(String fullName, String bio, String profilePictureUrl,
                              LocalDate dateOfBirth, Gender gender, UUID defaultAddressId) {
        this.fullName = fullName;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.defaultAddressId = defaultAddressId;
        this.setUpdatedAt(Instant.now());
    }

    public void updateBasicInfo(String fullName, String bio, String profilePictureUrl,
                                 LocalDate dateOfBirth, Gender gender) {
        this.fullName = fullName;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.setUpdatedAt(Instant.now());
    }

    public boolean hasProfilePicture() {
        return profilePictureUrl != null && !profilePictureUrl.isEmpty();
    }

    public boolean isProfileCompleted() {
        return fullName != null
                && gender != null
                && dateOfBirth != null;
    }
}
