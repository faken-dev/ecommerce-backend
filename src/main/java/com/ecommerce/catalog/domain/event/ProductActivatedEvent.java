package com.ecommerce.catalog.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ProductActivatedEvent(UUID productId, Instant occurredAt) {}
