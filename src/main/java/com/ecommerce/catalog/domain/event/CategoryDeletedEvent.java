package com.ecommerce.catalog.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CategoryDeletedEvent(UUID categoryId, Instant occurredAt) {}
