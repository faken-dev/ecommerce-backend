-- V18__shared_media_assets.sql
-- Table to track all media uploads and manage their lifecycle (Orphan cleanup)

CREATE TABLE shared_media_assets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    url             TEXT NOT NULL,
    storage_key     TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    file_size       BIGINT,
    mime_type       VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_media_assets_status ON shared_media_assets(status);
CREATE INDEX idx_media_assets_url ON shared_media_assets(url);
CREATE INDEX idx_media_assets_created ON shared_media_assets(created_at);
