package com.ecommerce.payment.application.command;

import java.util.UUID;

/**
 * Command for admin to reject a refund request.
 */
public record RejectRefundCommand(
    UUID refundId,
    UUID paymentId,
    String reason
) {}
