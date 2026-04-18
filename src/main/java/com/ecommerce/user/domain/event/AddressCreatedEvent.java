package com.ecommerce.user.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AddressCreatedEvent(
        UUID addressId,
        UUID userId,
        String fullAddress,
        boolean isDefault,
        Instant occurredAt
) {}
