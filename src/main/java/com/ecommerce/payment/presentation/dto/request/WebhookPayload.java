package com.ecommerce.payment.presentation.dto.request;

/**
 * Request body for gateway webhook endpoints.
 * The signature is validated via the PaymentGatewayPort.
 */
public record WebhookPayload(
    String provider,
    String eventType,
    String providerReference,
    String paymentIdExternal,
    String status,
    String failureCode,
    String failureReason,
    Long paidAtEpochMs
) {}
