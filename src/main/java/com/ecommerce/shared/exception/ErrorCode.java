package com.ecommerce.shared.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Generic
    INTERNAL_SERVER_ERROR(1001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT(1002, "Invalid input", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(1003, "Resource not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1004, "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1005, "Forbidden", HttpStatus.FORBIDDEN),
    METHOD_NOT_ALLOWED(1006, "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    CONFLICT(1007, "Conflict", HttpStatus.CONFLICT),

    // Authentication & Security
    AUTH_INVALID_TOKEN(2001, "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    AUTH_USER_NOT_FOUND(2002, "User not found", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_CREDENTIALS(2003, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_BLOCKED(2004, "Account has been blocked", HttpStatus.FORBIDDEN),
    AUTH_EMAIL_ALREADY_EXISTS(2005, "Email already exists", HttpStatus.CONFLICT),
    AUTH_PHONE_ALREADY_EXISTS(2006, "Phone number already exists", HttpStatus.CONFLICT),
    AUTH_USER_DELETED(2007, "User has been deleted", HttpStatus.UNAUTHORIZED),
    AUTH_EMAIL_NOT_VERIFIED(2008, "Email not verified", HttpStatus.FORBIDDEN),
    AUTH_OTP_BLOCKED(2009, "OTP sending is temporarily blocked", HttpStatus.FORBIDDEN),
    AUTH_REFRESH_TOKEN_REVOKED(2010, "Refresh token has been revoked", HttpStatus.UNAUTHORIZED),

    // OTP
    OTP_EXPIRED(3001, "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_ALREADY_USED(3002, "OTP has already been used", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_EXCEEDED(3003, "Maximum verification attempts exceeded", HttpStatus.BAD_REQUEST),
    OTP_INVALID(3004, "Invalid OTP code", HttpStatus.BAD_REQUEST),

    // Rate limiting
    RATE_LIMIT_EXCEEDED(4290, "Too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS),

    // User
    USER_NOT_FOUND(5003, "User not found", HttpStatus.NOT_FOUND),
    USER_PROFILE_NOT_FOUND(5001, "User profile not found", HttpStatus.NOT_FOUND),
    USER_INVALID_DATE_OF_BIRTH(5002, "Date of birth cannot be in the future", HttpStatus.BAD_REQUEST),

    // Catalog
    PRODUCT_NOT_FOUND(6001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_OUT_OF_STOCK(6002, "Product is out of stock", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_ACTIVE(6003, "Product is not active", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(6004, "Category not found", HttpStatus.NOT_FOUND),
    CATALOG_INVALID_SLUG(6005, "Invalid slug", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(6006, "Validation failed", HttpStatus.BAD_REQUEST),

    // Order
    ORDER_NOT_FOUND(7001, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_CANNOT_BE_CANCELLED(7002, "Order cannot be cancelled in its current status", HttpStatus.BAD_REQUEST),
    CART_NOT_FOUND(7003, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(7004, "Cart item not found", HttpStatus.NOT_FOUND),

    // Payment
    PAYMENT_REQUIRED(8001, "Payment required", HttpStatus.PAYMENT_REQUIRED),
    PAYMENT_FAILED(8002, "Payment failed", HttpStatus.BAD_REQUEST),

    // Voucher
    VOUCHER_NOT_FOUND(9001, "Voucher not found", HttpStatus.NOT_FOUND),
    VOUCHER_EXPIRED(9002, "Voucher has expired", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_YET_ACTIVE(9003, "Voucher is not yet active", HttpStatus.BAD_REQUEST),
    VOUCHER_USAGE_LIMIT_REACHED(9004, "Voucher usage limit reached", HttpStatus.BAD_REQUEST),
    VOUCHER_USER_LIMIT_REACHED(9005, "You have already used this voucher", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_SPEND_NOT_MET(9006, "Minimum spend not met for this voucher", HttpStatus.BAD_REQUEST),
    VOUCHER_MAX_SPEND_EXCEEDED(9007, "Maximum spend exceeded for this voucher", HttpStatus.BAD_REQUEST),
    VOUCHER_INACTIVE(9008, "Voucher is currently inactive", HttpStatus.BAD_REQUEST),
    VOUCHER_CODE_ALREADY_EXISTS(9009, "A voucher with this code already exists", HttpStatus.CONFLICT),
    VOUCHER_NOT_APPLICABLE(9010, "Voucher is not applicable to this order", HttpStatus.BAD_REQUEST),
    VOUCHER_SCOPE_MISMATCH(9011, "No eligible products or categories in this order for the voucher", HttpStatus.BAD_REQUEST),
    VOUCHER_FORBIDDEN(9012, "You do not have permission to access this voucher", HttpStatus.FORBIDDEN),
    VOUCHER_DEPLETED(9013, "Voucher is out of stock", HttpStatus.BAD_REQUEST),
    VOUCHER_ALREADY_COLLECTED(9014, "Voucher already collected", HttpStatus.BAD_REQUEST),
    VOUCHER_INVALID(9015, "Voucher is invalid", HttpStatus.BAD_REQUEST),

    // Shipping
    SHIPMENT_ALREADY_EXISTS(10001, "Shipment already exists for this order", HttpStatus.CONFLICT),
    SHIPMENT_NOT_FOUND(10002, "Shipment not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}