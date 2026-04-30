-- V11__feedback_and_messaging.sql
-- Reviews, Notifications, and Outbox.

CREATE TABLE user_notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    action_url VARCHAR(255),
    metadata TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT
);

CREATE TABLE feedback_reviews (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    user_id UUID NOT NULL,
    order_id UUID,
    rating INT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT
);

CREATE TABLE feedback_review_images (
    review_id UUID NOT NULL REFERENCES feedback_reviews(id) ON DELETE CASCADE,
    image_url VARCHAR(255) NOT NULL
);

CREATE TABLE feedback_review_comments (
    id UUID PRIMARY KEY,
    review_id UUID NOT NULL REFERENCES feedback_reviews(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    parent_comment_id UUID,
    content TEXT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT
);

CREATE TABLE shared_outbox_events (
    id                UUID PRIMARY KEY,
    aggregate_type    VARCHAR(100) NOT NULL,
    aggregate_id      VARCHAR(100) NOT NULL,
    event_type        VARCHAR(255) NOT NULL,
    payload           TEXT NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at      TIMESTAMPTZ,
    error             TEXT,
    version           BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_user_notifications_user ON user_notifications(user_id);
CREATE INDEX idx_feedback_reviews_product ON feedback_reviews(product_id);
CREATE INDEX idx_shared_outbox_pending ON shared_outbox_events(status, created_at ASC) WHERE status = 'PENDING';
