package com.ecommerce.order.application.command;

import java.util.UUID;

public record UpdateOrderStatusCommand(
        UUID orderId,
        String newStatus,
        String reason,
        String metadata
) {}
