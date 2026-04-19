-- V7__catelog_init.sql
-- Defines core catalog schema for e-commerce product management.
-- Includes category hierarchy, products with pricing/stock/SEO, product images, and variant support.

CREATE TABLE catalog_categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug            VARCHAR(100) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    parent_id       UUID REFERENCES catalog_categories(id),
    icon_url        VARCHAR(500),
    sort_order      INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID,
    updated_by      UUID,

    CONSTRAINT catalog_categories_slug_unique UNIQUE (slug)
);

CREATE INDEX idx_catalog_categories_parent ON catalog_categories(parent_id);
CREATE INDEX idx_catalog_categories_active ON catalog_categories(is_active);
CREATE INDEX idx_catalog_categories_slug ON catalog_categories(slug);
CREATE INDEX idx_catalog_categories_sort ON catalog_categories(sort_order);

-- Products
CREATE TABLE catalog_products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id       UUID NOT NULL,

    -- Basic info
    name            VARCHAR(300) NOT NULL,
    slug            VARCHAR(350) NOT NULL,
    description     TEXT,

    -- Pricing & Stock
    price           DECIMAL(19,4) NOT NULL CHECK (price >= 0),
    compare_at_price DECIMAL(19,4) CHECK (compare_at_price IS NULL OR compare_at_price >= 0),
    cost_per_item   DECIMAL(19,4) CHECK (cost_per_item IS NULL OR cost_per_item >= 0),
    stock_quantity  INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 10,

    -- SKU & Barcode
    sku             VARCHAR(100),
    barcode         VARCHAR(100),

    -- Categorisation
    category_id     UUID REFERENCES catalog_categories(id),
    tags            TEXT,

    -- Visibility / Status
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    is_featured     BOOLEAN NOT NULL DEFAULT FALSE,
    visibility      VARCHAR(20) NOT NULL DEFAULT 'SHOP',

    -- SEO
    meta_title      VARCHAR(70),
    meta_description VARCHAR(160),

    -- Rating cache (denormalised for query performance)
    average_rating  DECIMAL(3,2) DEFAULT 0 CHECK (average_rating >= 0 AND average_rating <= 5),
    review_count    INT NOT NULL DEFAULT 0,

    -- Weight / Shipping (nullable for digital products)
    weight_kg       DECIMAL(10,4),
    weight_unit     VARCHAR(10) DEFAULT 'KG',

    -- Soft delete
    deleted_at      TIMESTAMPTZ,

    -- Audit
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID,
    updated_by      UUID,

    CONSTRAINT catalog_products_slug_unique UNIQUE (seller_id, slug)
);

CREATE INDEX idx_catalog_products_seller   ON catalog_products(seller_id);
CREATE INDEX idx_catalog_products_category ON catalog_products(category_id);
CREATE INDEX idx_catalog_products_status   ON catalog_products(status);
CREATE INDEX idx_catalog_products_slug     ON catalog_products(slug);
CREATE INDEX idx_catalog_products_price    ON catalog_products(price);
CREATE INDEX idx_catalog_products_sku      ON catalog_products(sku) WHERE sku IS NOT NULL;
CREATE INDEX idx_catalog_products_active   ON catalog_products(seller_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_catalog_products_featured ON catalog_products(is_featured) WHERE status = 'ACTIVE';

-- Product Images
CREATE TABLE catalog_product_images (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID NOT NULL REFERENCES catalog_products(id) ON DELETE CASCADE,
    url             VARCHAR(500) NOT NULL,
    alt_text        VARCHAR(200),
    sort_order      INT NOT NULL DEFAULT 0,
    width           INT,
    height          INT,
    file_size_kb    INT,
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID,
    updated_by      UUID
);

CREATE INDEX idx_catalog_product_images_product ON catalog_product_images(product_id);
CREATE INDEX idx_catalog_product_images_sort   ON catalog_product_images(product_id, sort_order);

-- Product Variants (e.g. Size M / Color Red)
CREATE TABLE catalog_product_variants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID NOT NULL REFERENCES catalog_products(id) ON DELETE CASCADE,

    -- Variant identity
    sku             VARCHAR(100),
    barcode         VARCHAR(100),
    title           VARCHAR(200) NOT NULL,

    -- Override parent pricing/stock (nullable)
    price           DECIMAL(19,4) CHECK (price IS NULL OR price >= 0),
    compare_at_price DECIMAL(19,4),
    stock_quantity  INT NOT NULL DEFAULT 0,

    -- Option key/value (e.g. "color=red", "size=m")
    option_name     VARCHAR(50) NOT NULL,
    option_value    VARCHAR(100) NOT NULL,
    option2_name    VARCHAR(50),
    option2_value   VARCHAR(100),

    -- Image
    image_url       VARCHAR(500),

    -- Status
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INT NOT NULL DEFAULT 0,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID,
    updated_by      UUID,

    CONSTRAINT catalog_product_variants_sku_unique UNIQUE (product_id, sku)
);

CREATE INDEX idx_catalog_product_variants_product ON catalog_product_variants(product_id);
CREATE INDEX idx_catalog_product_variants_sku     ON catalog_product_variants(sku) WHERE sku IS NOT NULL;
CREATE INDEX idx_catalog_product_variants_active  ON catalog_product_variants(product_id, is_active);