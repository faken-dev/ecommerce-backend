package com.ecommerce.shared.domain.valueobject;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money Value Object - encapsulation of amount and currency.
 * Ensures arithmetic operations are only performed on consistent currencies.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "Amount is required");
        Objects.requireNonNull(currency, "Currency is required");
        
        // Ensure standard scaling (2 decimal places for most currencies, 0 for VND)
        if (currency.equals("VND")) {
            amount = amount.setScale(0, RoundingMode.HALF_UP);
        } else {
            amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        checkCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        checkCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        checkCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        checkCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private void checkCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, 
                    "Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }
}
