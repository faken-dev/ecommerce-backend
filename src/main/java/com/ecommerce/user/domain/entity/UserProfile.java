package com.ecommerce.user.domain.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.user.domain.event.UserProfileCreatedEvent;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserProfile extends AuditableEntity {
    public enum Gender { MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY }

    private UUID userId;
    private String fullName;
    private String profilePictureUrl;
    private String bio;
    private LocalDate dateOfBirth;
    private Gender gender;
    private UUID defaultAddressId;

    public static UserProfile createFromRegistration(UUID userId, String fullName) {
        return UserProfile.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .userId(userId)
                .fullName(fullName)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void updateProfile(String fullName, String profilePictureUrl, String bio,
                                LocalDate dateOfBirth, Gender gender, UUID defaultAddressId) {
        this.fullName = fullName;   
        this.profilePictureUrl = profilePictureUrl;
        this.bio = bio;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.defaultAddressId = defaultAddressId;
        this.touchUpdate();
    }

    public int calculateAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    public boolean isAdult() {
        return calculateAge() >= 18;
    }

    public boolean hasProfilePicture() {
        return profilePictureUrl != null && !profilePictureUrl.isEmpty();
    }

    public boolean isProfileCompleted() {
        return fullName != null
                && gender != null
                && dateOfBirth != null
                && profilePictureUrl != null;
    }

    public void updateBasicInfo(String fullName, String bio, 
                                 LocalDate dateOfBirth, Gender gender) {
        this.fullName = fullName;
        this.bio = bio;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.touchUpdate();
    }

    public void updateAvatar(String newProfilePictureUrl) {
        this.profilePictureUrl = newProfilePictureUrl;
        this.touchUpdate();
    }

    public UserProfileCreatedEvent toCreatedEvent() {
        return new UserProfileCreatedEvent(getId(), userId, fullName, Instant.now());
    }
}
