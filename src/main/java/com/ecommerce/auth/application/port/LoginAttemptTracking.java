package com.ecommerce.auth.application.port;

import java.time.Duration;
import java.util.UUID;

/**
 * Tracks failed login attempts per user for account lockout.
 *
 * Lockout policy:
 * - After {@code maxAttempts} consecutive failed attempts → account is temporarily locked.
 * - Lockout duration increases with each subsequent lockout (exponential backoff).
 * - Successful login resets the attempt counter.
 * - Duration: starts at 1 minute, doubles each time (max 30 minutes).
 */
public interface LoginAttemptTracking {

    /**
     * Records a failed login attempt. Increments counter and applies lockout if threshold reached.
     * @param userId   the user who failed to authenticate
     * @param duration the base lockout duration (will be multiplied by lockout count)
     */
    void recordFailedAttempt(UUID userId, Duration duration);

    /**
     * Records a successful login — resets the failure counter for this user.
     * @param userId the user who authenticated successfully
     */
    void recordSuccessfulLogin(UUID userId);

    /**
     * @return true if the account is currently locked due to failed attempts
     */
    boolean isLocked(UUID userId);

    /**
     * @return remaining lockout time in seconds (0 if not locked)
     */
    long getRemainingLockoutSeconds(UUID userId);

    /**
     * Manually unlocks an account (admin action).
     * @param userId the user to unlock
     */
    void unlock(UUID userId);
}
