-- Migration V9: Payment System Initialization
-- Tables for payment processing and linkage to the order module.

CREATE TABLE payment_payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    buyer_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    refunded_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    provider VARCHAR(20) NOT NULL,
    method_type VARCHAR(20) NOT NULL,
    provider_reference VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    failure_code VARCHAR(50),
    description VARCHAR(500),
    return_url VARCHAR(1000),
    cancel_url VARCHAR(1000),
    paid_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    expired_at TIMESTAMPTZ,
    idempotency_key VARCHAR(255) UNIQUE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE payment_refunds (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payment_payments(id),
    order_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    provider_refund_id VARCHAR(255),
    ip_address VARCHAR(45),
    approved_at TIMESTAMPTZ,
    rejected_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- Link orders to their successful payment ID
ALTER TABLE orders_orders ADD COLUMN payment_id UUID;

CREATE INDEX idx_payment_payments_order ON payment_payments(order_id);
CREATE INDEX idx_payment_payments_buyer ON payment_payments(buyer_id);
CREATE INDEX idx_payment_payments_status ON payment_payments(status);
CREATE INDEX idx_payment_refunds_payment ON payment_refunds(payment_id);
CREATE INDEX idx_orders_payment_id ON orders_orders(payment_id);
