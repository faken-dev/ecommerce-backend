package com.ecommerce.auth.application.port;

public interface TokenBlacklistService {

    /**
     * Add an access token to the blacklist with the given TTL.
     *
     * @param accessToken raw JWT token
     * @param ttlMs       remaining token lifetime in milliseconds
     */
    void blacklist(String accessToken, long ttlMs);

    /**
     * Check whether the token is blacklisted (revoked).
     *
     * @param accessToken raw JWT token
     * @return true if token is revoked
     */
    boolean isBlacklisted(String accessToken);
}