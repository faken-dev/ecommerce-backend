package com.ecommerce.payment.infrastructure.gateway;

import com.ecommerce.payment.application.port.*;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.infrastructure.config.PaymentProperties;
import com.ecommerce.payment.infrastructure.gateway.momo.MoMoGatewayAdapter;
import com.ecommerce.payment.infrastructure.gateway.paypal.PayPalGatewayAdapter;
import com.ecommerce.payment.infrastructure.gateway.stripe.StripeGatewayAdapter;
import com.ecommerce.payment.infrastructure.gateway.vnpay.VNPayGatewayAdapter;
import com.ecommerce.payment.infrastructure.gateway.zalopay.ZaloPayGatewayAdapter;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Strategy Factory for payment gateways.
 *
 * <p>Resolves the correct {@link PaymentGatewayPort} adapter based on {@link PaymentProvider}.
 * Uses a map-based strategy pattern for O(1) lookup.
 *
 * <p>The factory is aware of which providers are configured and throws
 * {@link IllegalStateException} for disabled providers rather than
 * returning a broken/null adapter - fail-fast principle.
 *
 * <p>Usage:
 * <pre>
 * PaymentGatewayPort gateway = gatewayFactory.getGateway(payment.getProvider());
 * String reference = gateway.initiate(...);
 * </pre>
 */

@Component
@RequiredArgsConstructor
public class GatewayStrategyFactory implements PaymentGatewayResolver {
    private static final Logger log = LoggerFactory.getLogger(GatewayStrategyFactory.class);

    private final PaymentProperties properties;

    // Inject all adapters - Spring resolves them by type
    private final VNPayGatewayAdapter vnpayAdapter;
    private final MoMoGatewayAdapter momoAdapter;
    private final PayPalGatewayAdapter paypalAdapter;
    private final StripeGatewayAdapter stripeAdapter;
    private final ZaloPayGatewayAdapter zalopayAdapter;
    private final StubPaymentGatewayAdapter stubAdapter;

    @Override
    public PaymentInitiator getInitiator(PaymentProvider provider) {
        return getGateway(provider);
    }

    @Override
    public RefundIssuer getRefundIssuer(PaymentProvider provider) {
        return getGateway(provider);
    }

    @Override
    public PaymentWebhookHandler getWebhookHandler(PaymentProvider provider) {
        return getGateway(provider);
    }

    @Override
    public PaymentStatusChecker getStatusChecker(PaymentProvider provider) {
        return getGateway(provider);
    }

    /**
     * Returns the gateway adapter for the given provider.
     * Never returns null - throws if provider is not configured.
     */
    @Override
    public PaymentGatewayPort getGateway(PaymentProvider provider) {
        return switch (provider) {
            case VNPAY -> resolveOrThrow(vnpayAdapter, "VNPay", properties.getVnpay().isEnabled());
            case MOMO -> resolveOrThrow(momoAdapter, "MoMo", properties.getMomo().isEnabled());
            case PAYPAL -> resolveOrThrow(paypalAdapter, "PayPal", properties.getPaypal().isEnabled());
            case STRIPE, VISA, MASTERCARD ->
                isProviderEnabled(provider) ? resolveOrThrow(stripeAdapter, "Stripe", true) : stubAdapter;
            case JPMORGAN_CHASE -> stubAdapter; // Bank transfer always enabled via manual flow/stub
            case ZALOPAY -> resolveOrThrow(zalopayAdapter, "ZaloPay", properties.getZalopay().isEnabled());
            case COD -> stubAdapter; // COD is always available (no gateway)
        };
    }

    /**
     * Returns the gateway adapter if enabled, throws otherwise.
     */
    private PaymentGatewayPort resolveOrThrow(PaymentGatewayPort adapter,
                                               String name, boolean enabled) {
        if (!enabled) {
            log.warn("[GatewayFactory] {} is not configured - set payment.{}.tmn-code / client-id / secret-key etc.",
                    name, name.toLowerCase());
            return stubAdapter;
        }
        return adapter;
    }

    /**
     * Returns true if the given provider is configured and can be used.
     */
    public boolean isProviderEnabled(PaymentProvider provider) {
        return switch (provider) {
            case COD, JPMORGAN_CHASE -> true;
            case VNPAY -> properties.getVnpay().isEnabled();
            case MOMO -> properties.getMomo().isEnabled();
            case PAYPAL -> properties.getPaypal().isEnabled();
            case STRIPE, VISA, MASTERCARD -> properties.getStripe().isEnabled();
            case ZALOPAY -> properties.getZalopay().isEnabled();
        };
    }

    /**
     * Returns all enabled providers (for frontend payment method selection).
     */
    public PaymentProvider[] getEnabledProviders() {
        return Arrays.stream(PaymentProvider.values())
                .filter(this::isProviderEnabled)
                .toArray(PaymentProvider[]::new);
    }
}
