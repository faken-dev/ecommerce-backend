package com.ecommerce.payment.application.port;

/**
 * Composite port for external payment gateways.
 * 
 * <p>Deprecated: Use {@link PaymentInitiator}, {@link RefundIssuer}, or 
 * {@link PaymentWebhookHandler} directly where possible to satisfy the 
 * Interface Segregation Principle (ISP).
 */
public interface PaymentGatewayPort extends PaymentInitiator, RefundIssuer, PaymentWebhookHandler, PaymentStatusChecker {
}
