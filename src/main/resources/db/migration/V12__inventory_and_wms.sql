-- V12__inventory_and_wms.sql
-- Warehouse Management System and stock handling.

CREATE TABLE inventory_warehouses (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ
);

CREATE TABLE inventory_zones (
    id UUID PRIMARY KEY,
    warehouse_id UUID NOT NULL REFERENCES inventory_warehouses(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ
);

CREATE TABLE inventory_slots (
    id UUID PRIMARY KEY,
    zone_id UUID NOT NULL REFERENCES inventory_zones(id),
    name VARCHAR(100) NOT NULL,
    capacity INT DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ
);

CREATE TABLE inventory_items (
    id UUID PRIMARY KEY,
    slot_id UUID REFERENCES inventory_slots(id),
    product_id UUID NOT NULL,
    variant_id UUID,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 10,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE inventory_logs (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    change_amount INT NOT NULL,
    stock_after INT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    reason TEXT,
    operator_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT
);

CREATE INDEX idx_inventory_warehouses_seller ON inventory_warehouses(seller_id);
CREATE INDEX idx_inventory_items_product ON inventory_items(product_id);
CREATE INDEX idx_inventory_logs_product ON inventory_logs(product_id);

-- Refactor Catalog: Remove legacy stock fields
ALTER TABLE catalog_products DROP COLUMN IF EXISTS stock_quantity;
ALTER TABLE catalog_products DROP COLUMN IF EXISTS low_stock_threshold;
ALTER TABLE catalog_products ADD COLUMN IF NOT EXISTS three_d_model_url TEXT;

ALTER TABLE catalog_product_variants DROP COLUMN IF EXISTS stock_quantity;
