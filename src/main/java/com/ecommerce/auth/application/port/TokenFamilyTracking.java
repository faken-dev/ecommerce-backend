package com.ecommerce.auth.application.port;

import java.util.UUID;

public interface TokenFamilyTracking {

    /**
     * Store the new token family generation after refresh.
     */
    void recordGeneration(UUID userId, long generation);

    /**
     * Check if the token generation matches the current family generation.
     *
     * @return true if valid, false if replay/old token detected
     */
    boolean isGenerationValid(UUID userId, long generation);

    /**
     * @return current token family generation (0 if none)
     */
    long getCurrentGeneration(UUID userId);

    /**
     * Revoke the entire token family (logout all sessions).
     */
    void revokeFamily(UUID userId);
}
