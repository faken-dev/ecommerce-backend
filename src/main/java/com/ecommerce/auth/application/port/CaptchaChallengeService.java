package com.ecommerce.auth.application.port;

import java.util.UUID;

/**
 * Port for bot/captcha challenge decisions.
 *
 * Determined by login attempt history and behavioral signals:
 * - After N failed attempts → challenge required
 * - If suspicious activity detected (e.g. too many attempts from same IP in short window)
 * - Challenge must be solved before password verification proceeds.
 */
public interface CaptchaChallengeService {

    /**
     * @param userId  the user attempting to login (null if email not found yet)
     * @param ipAddress client IP address
     * @param email   attempted email
     * @return true if a captcha challenge should be presented before verifying credentials
     */
    boolean isCaptchaRequired(UUID userId, String ipAddress, String email);

    /**
     * Verify the captcha token returned by the client.
     * @param captchaToken token from the captcha challenge
     * @param ipAddress client IP
     * @return true if token is valid
     */
    boolean verifyCaptcha(String captchaToken, String ipAddress);
}