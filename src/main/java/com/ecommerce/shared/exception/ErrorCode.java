package com.ecommerce.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Generic
    INTERNAL_SERVER_ERROR(5000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED(4000, "Validation failed", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(4004, "Resource not found", HttpStatus.NOT_FOUND),
    ACCESS_DENIED(4003, "Access denied", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(4001, "Unauthorized", HttpStatus.UNAUTHORIZED),

    // Auth
    AUTH_INVALID_CREDENTIALS(4010, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    AUTH_EMAIL_ALREADY_EXISTS(4011, "Email already registered", HttpStatus.CONFLICT),
    AUTH_PHONE_ALREADY_EXISTS(4012, "Phone number already registered", HttpStatus.CONFLICT),
    AUTH_TOKEN_EXPIRED(4013, "Token has expired", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_INVALID(4014, "Token is invalid", HttpStatus.UNAUTHORIZED),
    AUTH_REFRESH_TOKEN_NOT_FOUND(4015, "Refresh token not found", HttpStatus.UNAUTHORIZED),
    AUTH_REFRESH_TOKEN_REVOKED(4016, "Refresh token has been revoked", HttpStatus.UNAUTHORIZED),

    // OTP
    OTP_INVALID(4020, "OTP is invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(4021, "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_EXCEEDED(4022, "Too many OTP attempts", HttpStatus.TOO_MANY_REQUESTS),
    OTP_ALREADY_USED(4023, "OTP has already been used", HttpStatus.BAD_REQUEST),

    // Rate Limiting
    RATE_LIMIT_EXCEEDED(4290, "Too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}