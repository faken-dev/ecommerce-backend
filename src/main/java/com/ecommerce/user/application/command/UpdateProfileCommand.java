package com.ecommerce.user.application.command;

import com.ecommerce.user.domain.entity.UserProfile;

import java.time.LocalDate;

public record UpdateProfileCommand(
        String fullName,
        String bio,
        LocalDate dateOfBirth,
        UserProfile.Gender gender,
        String profilePictureUrl
) {}