package com.ecommerce.payment.domain.event;

import java.util.UUID;

public record PaymentCancelledEvent(
        UUID paymentId,
        UUID orderId,
        UUID buyerId,
        UUID cancelledBy
) {}
