-- V7__user_create_addresses.sql
-- User shipping addresses with default-address support

CREATE TABLE addresses (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL,
    recipient_name  VARCHAR(255) NOT NULL,
    recipient_phone VARCHAR(20)  NOT NULL,
    address_line    VARCHAR(500) NOT NULL,
    ward            VARCHAR(255) NOT NULL,
    district        VARCHAR(255) NOT NULL,
    province        VARCHAR(255) NOT NULL,
    default_address BOOLEAN      NOT NULL DEFAULT false,

    -- Audit
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    updated_by      UUID
);

CREATE INDEX idx_addresses_user_id      ON addresses(user_id);
CREATE INDEX idx_addresses_user_default ON addresses(user_id, default_address)
    WHERE default_address = true;