-- V1__auth_create_roles_and_permissions.sql
-- Base auth tables: roles, permissions, role_permissions junction
-- Authorization is PERMISSION-based (not role-name checks).
-- Audit fields store actor UUID.

CREATE TABLE auth_roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    deleted_at  TIMESTAMPTZ
);

CREATE TABLE auth_permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    deleted_at  TIMESTAMPTZ
);

CREATE TABLE auth_role_permissions (
    role_id       UUID NOT NULL REFERENCES auth_roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES auth_permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- PERMISSIONS
-- Naming: <domain>:<action>  — action ∈ {create, read, update, delete, manage, apply}
-- ─────────────────────────────────────────────────────────────────────────────

-- Auth / User
INSERT INTO auth_permissions (name, description) VALUES
    ('user:create',            'Create new user accounts'),
    ('user:read',              'View user accounts'),
    ('user:update',            'Update user accounts'),
    ('user:delete',            'Soft-delete user accounts'),
    ('user:manage',            'Full user management (impersonate, ban, etc.)'),

    -- RBAC (admin only)
    ('role:create',            'Create new roles'),
    ('role:read',              'View roles'),
    ('role:update',            'Update role name/description'),
    ('role:delete',            'Soft-delete roles'),
    ('role:assign',            'Assign roles to users'),
    ('permission:create',      'Create new permissions'),
    ('permission:read',        'View permissions'),
    ('permission:update',      'Update permissions'),
    ('permission:delete',      'Soft-delete permissions'),

    -- Category
    ('category:create',        'Create categories'),
    ('category:read',          'View categories'),
    ('category:update',        'Update categories'),
    ('category:delete',        'Soft-delete categories'),

    -- Product
    ('product:read',           'View products'),
    ('product:create',         'Create products'),
    ('product:update',         'Update products'),
    ('product:delete',         'Soft-delete products'),
    ('product:activate',       'Activate / deactivate products'),
    ('product:stock',          'Adjust product stock'),
    ('product:manage',         'Full product management (all sellers products)'),

    -- Order
    ('order:read',             'View own orders'),
    ('order:create',           'Place orders'),
    ('order:cancel',           'Cancel own orders'),
    ('order:manage',           'Manage all orders (seller sees own, admin sees all)'),
    ('order:update-status',    'Update order status (confirm, ship, deliver, etc.)'),

    -- Payment
    ('payment:read',           'View own payments'),
    ('payment:create',         'Create payment records'),
    ('payment:cancel',         'Cancel own pending payments'),
    ('payment:request-refund', 'Request refund for own payments'),
    ('payment:refund',         'Approve/reject refunds'),
    ('payment:manage',         'Manage all payments'),
    ('payment:webhook',        'Handle payment gateway webhooks (gateway signature verified)'),

    -- Voucher
    ('voucher:read',           'View vouchers'),
    ('voucher:create',         'Create vouchers'),
    ('voucher:update',         'Update vouchers'),
    ('voucher:delete',         'Soft-delete vouchers'),
    ('voucher:apply',          'Apply voucher to own cart/order'),

    -- Notification
    ('notification:read',      'View own notifications'),
    ('notification:manage',    'Send / manage all notifications (admin/system)');

-- ─────────────────────────────────────────────────────────────────────────────
-- ROLES
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO auth_roles (name, description) VALUES
    ('ADMIN',  'Full system access — all permissions'),
    ('SELLER', 'Manage own catalog, orders, and self-applied vouchers'),
    ('BUYER',  'Browse, order, apply vouchers, manage own profile');

-- ─────────────────────────────────────────────────────────────────────────────
-- ROLE → PERMISSION ASSIGNMENTS
-- ADMIN: cross-join = all permissions (includes permission:create/update/delete)
-- ─────────────────────────────────────────────────────────────────────────────

-- ADMIN: all permissions (cross-join picks up permission:create/update/delete for RBAC)
INSERT INTO auth_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth_roles r, auth_permissions p WHERE r.name = 'ADMIN';

-- SELLER: catalog + orders + own vouchers
INSERT INTO auth_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth_roles r, auth_permissions p
WHERE r.name = 'SELLER' AND p.name IN (
    'category:read',
    'product:read', 'product:create', 'product:update', 'product:delete',
    'product:activate', 'product:stock',
    'order:read', 'order:manage', 'order:update-status',
    'payment:read', 'payment:manage', 'payment:webhook',
    'voucher:read', 'voucher:create', 'voucher:update', 'voucher:delete'
);

-- BUYER: read + self-actions
INSERT INTO auth_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth_roles r, auth_permissions p
WHERE r.name = 'BUYER' AND p.name IN (
    'category:read',
    'product:read',
    'order:read', 'order:create', 'order:cancel',
    'payment:read', 'payment:create', 'payment:cancel', 'payment:request-refund',
    'voucher:read', 'voucher:apply',
    'notification:read'
);