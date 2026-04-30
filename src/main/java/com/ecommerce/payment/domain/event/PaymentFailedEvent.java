package com.ecommerce.payment.domain.event;

import java.util.UUID;

public record PaymentFailedEvent(
        UUID paymentId,
        UUID orderId,
        UUID buyerId,
        String failureCode,
        String failureReason
) {}
