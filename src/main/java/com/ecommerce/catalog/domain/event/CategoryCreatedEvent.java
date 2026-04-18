package com.ecommerce.catalog.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CategoryCreatedEvent(UUID categoryId, Instant occurredAt) {}
