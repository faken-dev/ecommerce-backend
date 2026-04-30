package com.ecommerce.payment.application.command;

import java.util.UUID;

/**
 * Command to record a failed payment from gateway webhook.
 */
public record FailPaymentCommand(
    UUID paymentId,
    String failureCode,
    String failureReason
) {}
