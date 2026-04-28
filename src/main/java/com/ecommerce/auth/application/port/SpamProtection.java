package com.ecommerce.auth.application.port;

import com.ecommerce.auth.domain.entity.OtpToken;

import java.time.Duration;
import java.util.UUID;

/**
 * Handles OTP spam protection: cooldowns, penalties, daily limits, and blocking.
 */
public interface SpamProtection {

    // ── Cooldown ─────────────────────────────

    /**
     * Start cooldown after an OTP request (per user + purpose).
     * User cannot request another OTP until cooldown expires.
     */
    void setCooldownWindow(UUID userId, OtpToken.Purpose purpose, long cooldownSeconds);

    /**
     * @return remaining cooldown time in seconds (0 if none)
     */
    long getCooldownRemainingSeconds(UUID userId, OtpToken.Purpose purpose);

    // ── Penalty ──────────────────────────────

    /**
     * Start penalty window when spam behavior is detected (per user + purpose).
     */
    void setPenaltyWindow(UUID userId, OtpToken.Purpose purpose, Duration penaltyDuration);

    /**
     * @return remaining penalty time in seconds (0 if none)
     */
    long getPenaltyTtl(UUID userId, OtpToken.Purpose purpose);

    /**
     * Extend an active penalty window.
     * @return new remaining penalty time in seconds
     */
    long extendPenalty(UUID userId, OtpToken.Purpose purpose, Duration penaltyDuration);

    /**
     * @return number of penalty warnings for this user + purpose
     */
    int getPenaltyWarnCount(UUID userId, OtpToken.Purpose purpose);

    /**
     * Increase warning count when user requests OTP during penalty.
     */
    void incrementPenaltyWarnCount(UUID userId, OtpToken.Purpose purpose, long ttlSeconds);

    // ── Daily Limit ──────────────────────────

    /**
     * @return OTP send count today (per user + purpose)
     */
    int getDailySendCount(UUID userId, OtpToken.Purpose purpose);

    /**
     * Increase daily send count after successful OTP sending.
     * @return updated count
     */
    int incrementDailySendCount(UUID userId, OtpToken.Purpose purpose);

    // ── Blocking ─────────────────────────────

    /**
     * Block account due to excessive OTP abuse.
     */
    void blockAccount(UUID userId);

    /**
     * @return true if account is blocked
     */
    boolean isAccountBlocked(UUID userId);
}
