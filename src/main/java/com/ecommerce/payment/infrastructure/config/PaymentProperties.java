package com.ecommerce.payment.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Centralized payment gateway configuration properties.
 */
@Component
@ConfigurationProperties(prefix = "payment")
@Validated
@Getter
@Setter
public class PaymentProperties {

    private VNPay vnpay = new VNPay();
    private MoMo momo = new MoMo();
    private PayPal paypal = new PayPal();
    private Stripe stripe = new Stripe();
    private ZaloPay zalopay = new ZaloPay();
    private Reconciliation reconciliation = new Reconciliation();

    @NotBlank
    private String baseUrl = "http://localhost:8080";

    @NotBlank
    private String frontendUrl = "http://localhost:5173";

    @Positive
    private int defaultTimeoutMinutes = 15;

    @Getter
    @Setter
    public static class VNPay {
        private String tmnCode;
        private String hashSecret;
        private String apiUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        private String refundUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
        private String command = "pay";
        private String locale = "vn";
        private String currency = "VND";
        private String version = "2.1.0";

        public boolean isEnabled() {
            return tmnCode != null && hashSecret != null;
        }
    }

    @Getter
    @Setter
    public static class MoMo {
        private String partnerCode;
        private String accessKey;
        private String secretKey;
        private String apiUrl = "https://test-payment.momo.vn/v2/gateway/api/create";
        private String refundUrl = "https://test-payment.momo.vn/v2/gateway/api/refund";
        private String ipnUrl;
        private String paymentUrl = "https://test-payment.momo.vn/v2/gateway/app";
        private String environment = "SANDBOX";

        public boolean isEnabled() {
            return partnerCode != null && accessKey != null && secretKey != null;
        }
    }

    @Getter
    @Setter
    public static class PayPal {
        private String clientId;
        private String clientSecret;
        private String mode = "sandbox";
        private String apiUrl;
        private String webhookId;
        private String returnUrl;
        private String cancelUrl;

        public String getApiUrl() {
            if (apiUrl != null) return apiUrl;
            return "live".equals(mode)
                    ? "https://api-m.paypal.com"
                    : "https://api-m.sandbox.paypal.com";
        }

        public boolean isEnabled() {
            return clientId != null && clientSecret != null;
        }
    }

    @Getter
    @Setter
    public static class Stripe {
        private String secretKey;
        private String publishableKey;
        private String webhookSecret;
        private String clientId;
        private String apiUrl = "https://api.stripe.com";
        private String currency = "vnd";

        public boolean isEnabled() {
            return secretKey != null;
        }
    }

    @Getter
    @Setter
    public static class ZaloPay {
        private String appId;
        private String key1;
        private String key2;
        private String apiUrl = "https://sb-openapi.zalopay.vn";
        private String refundUrl = "https://sb-openapi.zalopay.vn/v2/refund";
        private String callbackUrl;

        public boolean isEnabled() {
            return appId != null && key1 != null && key2 != null;
        }
    }

    @Getter
    @Setter
    public static class Reconciliation {
        private long intervalMs = 900000;
        private int thresholdMinutes = 30;
    }
}
