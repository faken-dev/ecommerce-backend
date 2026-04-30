package com.ecommerce.auth.application.port;

import java.util.UUID;

/**
 * Port for retrieving user profile information from the user module.
 * This avoids direct coupling between Auth and User modules.
 */
public interface ProfileProvider {
    /**
     * Retrieves the profile picture URL for a given user.
     * 
     * @param userId the ID of the user
     * @return the profile picture URL, or null if not set
     */
    String getProfilePictureUrl(UUID userId);
}
