package com.ecommerce.order.application.port;

import java.util.UUID;

public interface PaymentQueryPort {
    UUID getPaymentIdByOrderId(UUID orderId);
}
