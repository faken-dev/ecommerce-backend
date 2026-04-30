package com.ecommerce.payment.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * High-level payment method categories.
 */
public enum PaymentMethodType {
    WALLET,       // MoMo, ZaloPay, PayPal, VNPay wallet
    CARD,         // Credit / Debit card (Visa, Mastercard, Amex)
    BANK_TRANSFER,// VietQR, Internet Banking
    COD;          // Cash on Delivery

    @JsonCreator
    public static PaymentMethodType fromString(String value) {
        if (value == null) return null;
        return switch (value.toUpperCase()) {
            case "CASH", "CASH_ON_DELIVERY" -> COD;
            case "E_WALLET", "DIGITAL_WALLET" -> WALLET;
            default -> {
                try {
                    yield valueOf(value.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown payment method type: " + value);
                }
            }
        };
    }
}
