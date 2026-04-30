package com.ecommerce.payment.application.command;

import java.time.Instant;
import java.util.UUID;

/**
 * Command for admin to approve a refund request.
 */
public record ApproveRefundCommand(
    UUID refundId,
    UUID paymentId,
    String providerRefundId,
    Instant approvedAt
) {}
