-- V5__user_create_profiles.sql
-- User profile extension: bio, avatar, date of birth, default shipping address

CREATE TABLE user_profiles (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID         NOT NULL UNIQUE,
    full_name            VARCHAR(255),
    profile_picture_url  VARCHAR(500),
    bio                  VARCHAR(1000),
    date_of_birth        DATE,
    gender               VARCHAR(20),
    default_address_id   UUID,

    -- Audit
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by           UUID,
    updated_by           UUID
);

CREATE INDEX idx_user_profiles_user_id      ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_default_addr ON user_profiles(default_address_id)
    WHERE default_address_id IS NOT NULL;