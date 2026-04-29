package com.ecommerce.auth.domain.valueobject;

/**
 * Identity provider for user authentication.
 * EMAIL = standard email/password + OTP login.
 * GOOGLE = Google OAuth2.
 * APPLE = Apple Sign In (future).
 */
public enum OAuth2Provider {
    EMAIL,
    GOOGLE,
    FACEBOOK,
    GITHUB,
    APPLE;

    /** Convenience: true for all OAuth providers (not standard email login). */
    public boolean isOAuth() {
        return this != EMAIL;
    }
}
