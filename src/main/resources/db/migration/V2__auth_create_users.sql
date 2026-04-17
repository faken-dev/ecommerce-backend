-- V2__auth_create_users.sql
-- Defines the auth_users table for user accounts, along with auth_user_roles junction for RBAC.
-- Audit fields store actor UUID for accountability.

CREATE TABLE auth_users (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Identity
    email                 VARCHAR(255) NOT NULL UNIQUE,
    phone_number          VARCHAR(20)  UNIQUE,
    full_name             VARCHAR(255) NOT NULL,
    password_hash         VARCHAR(255),

    -- Status
    is_active              BOOLEAN      NOT NULL DEFAULT true,
    is_email_verified      BOOLEAN      NOT NULL DEFAULT false,
    is_phone_verified      BOOLEAN      NOT NULL DEFAULT false,
    is_otp_blocked         BOOLEAN      NOT NULL DEFAULT false,
    otp_blocked_at        TIMESTAMPTZ,
    otp_blocked_reason    VARCHAR(255),

    -- OAuth2 identity (null = email/password login)
    provider              VARCHAR(20),
    provider_user_id     VARCHAR(255),

    -- Soft delete
    deleted_at           TIMESTAMPTZ,
    deleted_by           UUID,

    -- Audit
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by           UUID,
    updated_by          UUID
);

-- Many-to-many: User <-> Role
CREATE TABLE auth_user_roles (
    user_id UUID NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES auth_roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);


-- Indexes for efficient lookups
CREATE UNIQUE INDEX idx_auth_users_provider
    ON auth_users(provider, provider_user_id)
    WHERE provider IS NOT NULL;
CREATE INDEX idx_auth_users_email         ON auth_users(email);
CREATE INDEX idx_auth_users_phone_number  ON auth_users(phone_number);
CREATE INDEX idx_auth_users_deleted_at    ON auth_users(deleted_at)
    WHERE deleted_at IS NULL;
