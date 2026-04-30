package com.ecommerce.payment.application.command;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to confirm a payment from gateway webhook.
 */
public record ConfirmPaymentCommand(
    UUID paymentId,
    String providerReference,
    Instant paidAt
) {}
