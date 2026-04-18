package com.ecommerce.user.application.dto;

import com.ecommerce.user.domain.entity.UserProfile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        UUID userId,
        String fullName,
        String profilePictureUrl,
        String bio,
        LocalDate dateOfBirth,
        UserProfile.Gender gender,
        UUID defaultAddressId,
        int age,
        boolean profileCompleted,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProfileResponse from(UserProfile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getFullName(),
                profile.getProfilePictureUrl(),
                profile.getBio(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getDefaultAddressId(),
                profile.calculateAge(),
                profile.isProfileCompleted(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
