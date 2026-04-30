-- V15__audit_and_maintenance.sql
-- Audit Logs, Permissions, and Schema Refinements.

CREATE TABLE admin_action_logs (
    id UUID PRIMARY KEY,
    admin_id UUID NOT NULL,
    admin_email VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id UUID,
    user_name VARCHAR(255),
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(255),
    resource_id VARCHAR(255),
    payload TEXT,
    ip_address VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    error_message TEXT
);

CREATE INDEX idx_admin_action_logs_admin ON admin_action_logs(admin_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

-- Refinements (V14, V15)
ALTER TABLE auth_users ADD COLUMN IF NOT EXISTS profile_picture_url VARCHAR(500);
UPDATE auth_users SET phone_number = NULL WHERE phone_number = '';

-- Modular Dashboards Permissions
INSERT INTO auth_permissions (name, description) VALUES
    ('inventory:manage', 'Manage warehouses, stock, and inventory system-wide'),
    ('cms:manage',       'Manage banners, home page content, and other CMS elements'),
    ('dashboard:read',   'View system-wide admin dashboard statistics'),
    ('analytics:read',   'View system-wide sales and performance analytics');

INSERT INTO auth_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth_roles r, auth_permissions p 
WHERE r.name = 'ADMIN' AND p.name IN ('inventory:manage', 'cms:manage', 'dashboard:read', 'analytics:read');

INSERT INTO auth_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth_roles r, auth_permissions p 
WHERE r.name = 'SELLER' AND p.name = 'inventory:manage';
