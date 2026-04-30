-- V14__shipping_module.sql
-- Shipping and delivery management.

CREATE TABLE shipping_shipments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    tracking_number VARCHAR(255),
    carrier_name VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    recipient_name VARCHAR(255),
    phone VARCHAR(20),
    street VARCHAR(255),
    district VARCHAR(100),
    city VARCHAR(100),
    province VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    weight NUMERIC(38,2),
    shipping_fee NUMERIC(38,2),
    estimated_delivery_date TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_shipping_order_id ON shipping_shipments(order_id);
CREATE INDEX idx_shipping_tracking_number ON shipping_shipments(tracking_number);

-- Permissions
INSERT INTO auth_permissions (name, description) VALUES 
    ('shipping:manage', 'Full access to shipping management'),
    ('shipping:read',   'View shipment tracking information');

INSERT INTO auth_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth_roles r, auth_permissions p 
WHERE r.name = 'ADMIN' AND p.name IN ('shipping:manage', 'shipping:read');
