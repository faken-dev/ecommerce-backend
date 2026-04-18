package com.ecommerce.user.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AddressUpdatedEvent(UUID addressId, UUID userId, Instant occurredAt) {}
