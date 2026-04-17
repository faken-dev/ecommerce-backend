-- V3__auth_create_otp_tokens.sql
-- Defines auth_otp_tokens for one-time password codes used in email/phone verification and password
-- resets. Stores hashed codes, channels, purposes, and lifecycle info for security and audit.


CREATE TABLE auth_otp_tokens (
    id          UUID         PRIMARY KEY,

    user_id     UUID         NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    code_hash   VARCHAR(255) NOT NULL,
    channel     VARCHAR(20)  NOT NULL CHECK (channel IN ('EMAIL', 'SMS', 'WHATSAPP')),
    purpose     VARCHAR(30)  NOT NULL CHECK (purpose IN ('EMAIL_VERIFICATION', 'PHONE_VERIFICATION', 'PASSWORD_RESET', 'LOGIN')),

    -- Lifecycle
    expires_at     TIMESTAMPTZ NOT NULL,
    used_at        TIMESTAMPTZ,
    attempt_count  INTEGER     NOT NULL DEFAULT 0,

    -- Audit
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     UUID,
    updated_by     UUID
);


-- Index to quickly find active OTPs for a user and purpose, ordered by newest first (for verification and rate-limiting)
CREATE INDEX idx_auth_otp_user_purpose
    ON auth_otp_tokens(user_id, purpose, created_at DESC);