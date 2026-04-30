package com.ecommerce.payment.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to request a refund for a payment.
 */
public record RequestRefundCommand(
    UUID paymentId,
    UUID orderId,
    UUID requestedBy,
    BigDecimal amount,
    String reason,
    String ipAddress
) {}
