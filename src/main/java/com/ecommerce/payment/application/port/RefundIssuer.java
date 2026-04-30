package com.ecommerce.payment.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port for issuing refunds through external gateways.
 */
public interface RefundIssuer {

    String issueRefund(
            UUID refundId,
            String providerReference,
            BigDecimal amount,
            String currency
    );
}
