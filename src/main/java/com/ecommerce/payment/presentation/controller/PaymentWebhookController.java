package com.ecommerce.payment.presentation.controller;

import com.ecommerce.payment.application.port.PaymentWebhookHandler.WebhookResult;
import com.ecommerce.payment.domain.entity.PaymentProvider;
import com.ecommerce.payment.application.port.PaymentGatewayResolver;
import com.ecommerce.payment.infrastructure.webhook.IdempotentWebhookProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Central webhook controller for ALL payment gateways.
 */

@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
@Tag(name = "Payment Webhook", description = "Gateway webhook handlers")
public class PaymentWebhookController {
    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final PaymentGatewayResolver gatewayResolver;
    private final IdempotentWebhookProcessor processor;

    @Operation(summary = "Unified gateway webhook / IPN handler")
    @RequestMapping(value = "/{providerName}", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> handleWebhook(
            @PathVariable String providerName,
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestBody(required = false) String rawPayload,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request) {

        log.info("[Webhook/{}] Received IPN via {}", providerName, request.getMethod());

        try {
            PaymentProvider provider = PaymentProvider.valueOf(providerName.toUpperCase());
            var handler = gatewayResolver.getWebhookHandler(provider);

            String signature;
            String payload;

            if (provider == PaymentProvider.VNPAY) {
                // VNPay sends everything in query params, signature is vnp_SecureHash
                signature = allParams.get("vnp_SecureHash");
                payload = buildQueryString(allParams);
            } else {
                signature = getSignatureFromHeaders(provider, headers);
                payload = rawPayload;
            }

            if (!handler.verifyWebhookSignature(provider, payload, signature)) {
                log.warn("[Webhook/{}] Invalid signature - ignored", providerName);
                // VNPay expects a specific JSON response even for failed signature
                if (provider == PaymentProvider.VNPAY) {
                    return ResponseEntity.ok("{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}");
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("INVALID_SIGNATURE");
            }

            WebhookResult result = handler.parseWebhookEvent(provider, payload, signature);

            if (result.providerReference() != null) {
                processor.process(provider, result, payload);
            }

            String responseBody = handler.generateWebhookResponse(provider, result);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(responseBody);

        } catch (IllegalArgumentException e) {
            log.error("[Webhook] Unknown provider: {}", providerName);
            return ResponseEntity.badRequest().body("UNKNOWN_PROVIDER");
        } catch (Exception e) {
            log.error("[Webhook/{}] Processing failed", providerName, e);
            return ResponseEntity.internalServerError().body("ERROR: " + e.getMessage());
        }
    }

    private String buildQueryString(Map<String, String> params) {
        // VNPay requires parameters to be sorted alphabetically
        SortedMap<String, String> sortedParams = new TreeMap<>(params);
        return sortedParams.entrySet().stream()
                .filter(e -> !e.getKey().equals("vnp_SecureHash") && !e.getKey().equals("vnp_SecureHashType"))
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    private String getSignatureFromHeaders(PaymentProvider provider, Map<String, String> headers) {
        return switch (provider) {
            case VNPAY -> headers.get("x-vnpay-signature");
            case MOMO -> headers.get("x-momo-signature");
            case STRIPE -> headers.get("stripe-signature");
            case ZALOPAY -> headers.get("x-zalopay-signature");
            case PAYPAL -> {
                // PayPal uses multiple headers for verification
                String txId = headers.get("paypal-transmission-id");
                String txTime = headers.get("paypal-transmission-time");
                String certUrl = headers.get("paypal-cert-url");
                String authAlgo = headers.get("paypal-auth-algo");
                String sig = headers.get("paypal-transmission-sig");
                yield String.format("{\"id\":\"%s\",\"time\":\"%s\",\"cert\":\"%s\",\"algo\":\"%s\",\"sig\":\"%s\"}",
                        txId, txTime, certUrl, authAlgo, sig);
            }
            default -> null;
        };
    }
}
