package com.ecommerce.shared.domain.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {
    private final int errorCode;

    protected DomainException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
