package com.ecommerce.auth.domain.valueobject;

import java.util.regex.Pattern;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;

public record PhoneNumber(String value) {

    // E.164 format: +84901234567
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Phone number must not be blank");
        }
        value = value.trim();
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Phone number must be in E.164 format e.g. +84901234567");
        }
    }

    @Override
    public String toString() {
        return value;
    }

    public static PhoneNumber of(String value) {
        if (value == null || value.isBlank()) return null;
        return new PhoneNumber(value);
    }
}