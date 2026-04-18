package com.ecommerce.catalog.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductCreatedEvent(UUID productId, UUID sellerId, String name, BigDecimal price, Instant occurredAt) {}
