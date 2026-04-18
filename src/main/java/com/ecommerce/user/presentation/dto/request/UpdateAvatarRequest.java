package com.ecommerce.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Presentation-layer request to update avatar URL.
 */
public record UpdateAvatarRequest(
        @NotBlank(message = "Avatar URL is required")
        String avatarUrl
) {}