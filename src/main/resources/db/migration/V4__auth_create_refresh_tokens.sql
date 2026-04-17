-- V4__auth_create_refresh_tokens.sql
-- Defines auth_refresh_tokens for secure, auditable refresh token management in JWT-based auth.
-- Stores hashed tokens, device/IP info, lifecycle timestamps, and rotation links for security and audit.

CREATE TABLE auth_refresh_tokens (
    id          UUID         PRIMARY KEY,

    user_id     UUID         NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,

    device_info VARCHAR(500),
    ip_address  VARCHAR(45),

    -- Lifecycle
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    replaced_by UUID        REFERENCES auth_refresh_tokens(id) ON DELETE SET NULL,

    -- Generation (starts at 1, increments on each rotation; older gen = attack)
    generation  BIGINT      NOT NULL DEFAULT 1,

    -- Audit
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID
);

-- Indexes for efficient lookups and security checks
CREATE INDEX idx_auth_refresh_tokens_user_id      ON auth_refresh_tokens(user_id);
CREATE INDEX idx_auth_refresh_tokens_replaced_by  ON auth_refresh_tokens(replaced_by);
CREATE INDEX idx_auth_refresh_tokens_active
    ON auth_refresh_tokens(user_id, expires_at)
    WHERE revoked_at IS NULL;