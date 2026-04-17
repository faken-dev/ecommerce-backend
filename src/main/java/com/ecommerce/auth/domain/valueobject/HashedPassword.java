package com.ecommerce.auth.domain.valueobject;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;

public record HashedPassword(String value) {

    public HashedPassword {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Password hash must not be blank");
        }
    }

    public static HashedPassword of(String bcryptHash) {
        return new HashedPassword(bcryptHash);
    }

    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}