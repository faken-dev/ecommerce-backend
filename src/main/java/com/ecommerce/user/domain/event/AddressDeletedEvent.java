package com.ecommerce.user.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AddressDeletedEvent(
        UUID addressId,
        UUID userId,
        Instant occurredAt
) {}
