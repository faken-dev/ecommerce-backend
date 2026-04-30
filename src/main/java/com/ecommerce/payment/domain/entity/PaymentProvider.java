package com.ecommerce.payment.domain.entity;

/**
 * Payment gateway providers supported by the platform.
 */
public enum PaymentProvider {
    COD,          // Cash on Delivery - no external gateway
    PAYPAL,
    STRIPE,
    VNPAY,        // Vietnamese payment gateway
    MOMO,         // MoMo e-wallet
    ZALOPAY,      // ZaloPay
    VISA,         // Visa via Stripe
    MASTERCARD,   // Mastercard via Stripe
    JPMORGAN_CHASE; // Bank transfer via JPMorgan

    public boolean isOnline() {
        return this != COD;
    }

    /**
     * Determines PaymentMethodType based on provider.
     */
    public PaymentMethodType inferMethodType() {
        return switch (this) {
            case MOMO, ZALOPAY -> PaymentMethodType.WALLET;
            case COD -> PaymentMethodType.COD;
            case PAYPAL -> PaymentMethodType.WALLET;
            case VNPAY -> PaymentMethodType.BANK_TRANSFER;
            default -> PaymentMethodType.CARD;
        };
    }
}
