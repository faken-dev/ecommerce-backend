-- V10__voucher_system.sql
-- Combined migration for Voucher System and related Order/Cart refinements.
-- Includes: Original V10, V16 (Voucher code), V18 (Category ID in items), V19 (Voucher column fix), V20 (Cart metadata).

CREATE TABLE voucher_vouchers (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                    VARCHAR(50) NOT NULL UNIQUE,
    name                    VARCHAR(200) NOT NULL,
    description             TEXT,
    type                    VARCHAR(30) NOT NULL,
    scope                   VARCHAR(30) NOT NULL DEFAULT 'ALL',
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    discount_value          DECIMAL(19, 4) NOT NULL,
    max_discount_amount     DECIMAL(19, 4),
    min_order_amount        DECIMAL(19, 4) NOT NULL DEFAULT 0,
    max_usage_total         INT NOT NULL DEFAULT -1,
    max_usage_per_user      INT NOT NULL DEFAULT 1,
    valid_from              TIMESTAMPTZ,
    valid_to                TIMESTAMPTZ,
    applicable_product_ids  UUID[],
    applicable_category_ids UUID[],
    seller_id              UUID,
    current_usage_count    INT NOT NULL DEFAULT 0,
    requires_collection    BOOLEAN NOT NULL DEFAULT FALSE,
    expired_at             TIMESTAMPTZ,
    deleted_at             TIMESTAMPTZ,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by             UUID,
    updated_by             UUID,
    version                BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT voucher_vouchers_discount_positive CHECK (discount_value > 0)
);

CREATE TABLE voucher_usages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    voucher_id          UUID NOT NULL REFERENCES voucher_vouchers(id),
    user_id             UUID NOT NULL,
    order_id            UUID NOT NULL UNIQUE,
    discount_applied    DECIMAL(19, 4) NOT NULL,
    used_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by         UUID,
    updated_by         UUID
);

CREATE TABLE voucher_user_vouchers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    voucher_id UUID NOT NULL REFERENCES voucher_vouchers(id),
    collected_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_user_vouchers_user_voucher UNIQUE (user_id, voucher_id)
);

-- Refinements for Orders (V16, V18, V19)
ALTER TABLE orders_orders ADD COLUMN applied_voucher_code VARCHAR(50);
ALTER TABLE orders_order_items ADD COLUMN category_id UUID;

-- Refinements for Cart (V20)
ALTER TABLE orders_cart_items ADD COLUMN seller_id UUID;
ALTER TABLE orders_cart_items ADD COLUMN product_name VARCHAR(300);
ALTER TABLE orders_cart_items ADD COLUMN product_image_url VARCHAR(500);
ALTER TABLE orders_cart_items ADD COLUMN variant_title VARCHAR(200);

-- Indexes
CREATE INDEX idx_voucher_vouchers_code ON voucher_vouchers(code);
CREATE INDEX idx_voucher_vouchers_status ON voucher_vouchers(status);
CREATE INDEX idx_voucher_usages_voucher ON voucher_usages(voucher_id);
CREATE INDEX idx_voucher_user_vouchers_user ON voucher_user_vouchers(user_id);

-- Initial Voucher Permissions
INSERT INTO auth_permissions (name, description) VALUES 
    ('voucher:manage', 'Full voucher management (create, expire, delete)');

INSERT INTO auth_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth_roles r, auth_permissions p 
WHERE r.name = 'ADMIN' AND p.name = 'voucher:manage';
