package com.ecommerce.user.presentation.dto.request;

import com.ecommerce.user.domain.entity.UserProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Presentation-layer request to update user profile.
 */
public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,

        @Size(max = 1000, message = "Bio must not exceed 1000 characters")
        String bio,

        LocalDate dateOfBirth,

        UserProfile.Gender gender,

        String profilePictureUrl
) {}
