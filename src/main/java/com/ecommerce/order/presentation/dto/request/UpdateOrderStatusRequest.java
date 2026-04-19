package com.ecommerce.order.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Presentation-layer request to update order status (seller/admin).
 * Wraps UpdateOrderStatusCommand for DDD compliance.
 */
public record UpdateOrderStatusRequest(
        @NotNull(message = "newStatus is required")
        String newStatus,

        String reason,

        String metadata
) {}
