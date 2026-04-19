-- V8__orders_init.sql
-- Defines core orders schema for checkout and purchase flow.
-- Includes orders, order items snapshots, status history audit log, and persistent carts/cart items with indexes for fast queries.

CREATE TABLE orders_orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id            UUID NOT NULL,
    seller_id           UUID NOT NULL,

    -- Status machine
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    /*
      PENDING          – Awaiting payment confirmation
      CONFIRMED        – Paid, seller notified
      PROCESSING       – Seller preparing shipment
      SHIPPED          – Handed to carrier
      DELIVERED        – Confirmed delivery
      CANCELLED        – Cancelled (terminal)
      REFUNDED         – Full refund processed (terminal)
      PARTIALLY_REFUNDED – Partial refund processed (terminal)
    */

    shipping_address_id UUID NOT NULL,

    -- Pricing (snapshot at order time)
    subtotal            DECIMAL(19,4) NOT NULL,
    shipping_fee        DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount          DECIMAL(19,4) NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount        DECIMAL(19,4) NOT NULL,

    -- Currency & Payment
    currency            VARCHAR(3)  NOT NULL DEFAULT 'VND',
    payment_method      VARCHAR(30),
    payment_status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    /* PENDING | PAID | FAILED | REFUNDED | PARTIALLY_REFUNDED */

    buyer_note          TEXT,
    seller_note         TEXT,

    shipping_carrier    VARCHAR(100),
    tracking_number     VARCHAR(200),
    cancel_window_sec   INT NOT NULL DEFAULT 1800,

    ip_address          VARCHAR(45),
    user_agent          VARCHAR(500),

    -- Soft delete
    deleted_at          TIMESTAMPTZ,

    -- Audit
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          UUID,
    updated_by          UUID,

    CONSTRAINT orders_orders_total_positive CHECK (total_amount >= 0),
    CONSTRAINT orders_orders_status_check CHECK (status IN (
        'PENDING','CONFIRMED','PROCESSING','SHIPPED',
        'DELIVERED','CANCELLED','REFUNDED','PARTIALLY_REFUNDED'))
);

CREATE INDEX idx_orders_buyer        ON orders_orders(buyer_id);
CREATE INDEX idx_orders_seller       ON orders_orders(seller_id);
CREATE INDEX idx_orders_status       ON orders_orders(status);
CREATE INDEX idx_orders_created      ON orders_orders(created_at);
CREATE INDEX idx_orders_buyer_status ON orders_orders(buyer_id, status)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_orders_seller_status ON orders_orders(seller_id, status)
    WHERE deleted_at IS NULL;

-- ============================================================
-- Order line items
-- ============================================================
CREATE TABLE orders_order_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL REFERENCES orders_orders(id) ON DELETE CASCADE,

    product_id          UUID NOT NULL,
    product_name        VARCHAR(300) NOT NULL,
    product_sku         VARCHAR(100),
    product_image_url   VARCHAR(500),

    variant_id          UUID,
    variant_title       VARCHAR(200),

    quantity            INT NOT NULL CHECK (quantity > 0),
    unit_price          DECIMAL(19,4) NOT NULL,
    total_price         DECIMAL(19,4) NOT NULL,
    discount_amount     DECIMAL(19,4) NOT NULL DEFAULT 0,

    refunded_quantity   INT NOT NULL DEFAULT 0,
    refunded_amount     DECIMAL(19,4) NOT NULL DEFAULT 0,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          UUID,
    updated_by          UUID
);

CREATE INDEX idx_order_items_order   ON orders_order_items(order_id);
CREATE INDEX idx_order_items_product ON orders_order_items(product_id);

-- ============================================================
-- Order status history (audit log)
-- ============================================================
CREATE TABLE orders_status_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders_orders(id) ON DELETE CASCADE,
    from_status     VARCHAR(20),
    to_status       VARCHAR(20) NOT NULL,
    changed_by      UUID,
    changed_by_role VARCHAR(20),
    reason          TEXT,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID,
    updated_by      UUID
);

CREATE INDEX idx_status_history_order ON orders_status_history(order_id);
CREATE INDEX idx_status_history_date  ON orders_status_history(created_at DESC);

-- ============================================================
-- Carts (persistent, one per buyer)
-- ============================================================
CREATE TABLE orders_carts (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id   UUID NOT NULL,

    -- Optimistic locking version (prevents concurrent write clobbering)
    version    BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,

    CONSTRAINT orders_carts_buyer_unique UNIQUE (buyer_id)
);

-- ============================================================
-- Cart line items
-- ============================================================
CREATE TABLE orders_cart_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id     UUID NOT NULL REFERENCES orders_carts(id) ON DELETE CASCADE,
    product_id  UUID NOT NULL,
    variant_id  UUID,
    quantity    INT NOT NULL CHECK (quantity > 0),

    unit_price  DECIMAL(19,4) NOT NULL,

    added_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by  UUID,
    updated_by  UUID
);

CREATE INDEX idx_cart_items_cart    ON orders_cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON orders_cart_items(product_id);

