package com.ecommerce.shared.event.outbox;

public enum OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
