package com.ecommerce.shared.application.port;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Port for fetching exchange rates between currencies.
 */
public interface ExchangeRatePort {
    /**
     * Gets the current exchange rate from source to target currency.
     * Example: getRate("VND", "USD") might return 0.00004.
     */
    BigDecimal getRate(String sourceCurrency, String targetCurrency);

    /**
     * Converts an amount from source to target currency.
     */
    default BigDecimal convert(BigDecimal amount, String sourceCurrency, String targetCurrency) {
        if (sourceCurrency.equals(targetCurrency)) return amount;
        BigDecimal rate = getRate(sourceCurrency, targetCurrency);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
