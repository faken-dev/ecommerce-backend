package com.ecommerce.payment.application.port;

import com.ecommerce.payment.domain.entity.PaymentProvider;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Port for initiating payment sessions.
 */
public interface PaymentInitiator {
    
    InitiationResult initiate(
            UUID paymentId,
            UUID orderId,
            BigDecimal amount,
            String currency,
            PaymentProvider provider,
            String returnUrl,
            String cancelUrl,
            String ipAddress
    );

    record InitiationResult(
            String providerReference,
            String paymentPageUrl,
            String clientSecret,
            Map<String, String> extraData
    ) {
        public static InitiationResult redirect(String reference, String url) {
            return new InitiationResult(reference, url, null, null);
        }

        public static InitiationResult sdk(String reference, String secret) {
            return new InitiationResult(reference, null, secret, null);
        }
    }
}
