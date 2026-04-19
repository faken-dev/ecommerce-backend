package com.ecommerce.order.presentation.dto.request;

import java.util.UUID;

/**
 * Presentation-layer request to cancel an order.
 * Wraps CancelOrderCommand for DDD compliance.
 */
public record CancelOrderRequest(
        UUID orderId,
        String reason
) {}
