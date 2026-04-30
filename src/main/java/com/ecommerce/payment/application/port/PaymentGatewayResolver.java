package com.ecommerce.payment.application.port;

import com.ecommerce.payment.domain.entity.PaymentProvider;

/**
 * Port for resolving payment gateway components (initiator, refund issuer, etc.)
 * based on the provider.
 */
public interface PaymentGatewayResolver {
    
    PaymentInitiator getInitiator(PaymentProvider provider);
    
    RefundIssuer getRefundIssuer(PaymentProvider provider);
    
    PaymentWebhookHandler getWebhookHandler(PaymentProvider provider);
    
    PaymentStatusChecker getStatusChecker(PaymentProvider provider);

    /**
     * Helper to get the full gateway port (composite).
     */
    PaymentGatewayPort getGateway(PaymentProvider provider);
}
