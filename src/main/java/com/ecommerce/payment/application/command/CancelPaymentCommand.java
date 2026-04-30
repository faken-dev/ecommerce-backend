package com.ecommerce.payment.application.command;

import java.util.UUID;

/**
 * Command for buyer-initiated payment cancellation.
 */
public record CancelPaymentCommand(
    UUID paymentId,
    UUID cancelledBy
) {}
