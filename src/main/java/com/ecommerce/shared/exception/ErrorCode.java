package com.ecommerce.shared.exception;

import org.springframework.http.HttpStatus;

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
    AUTH_EMAIL_NOT_VERIFIED(4017, "Email not verified. Please check your inbox.", HttpStatus.FORBIDDEN),
    AUTH_OTP_BLOCKED(4018, "OTP feature is blocked. Please contact admin.", HttpStatus.FORBIDDEN),
    AUTH_ACCOUNT_BLOCKED(4019,"Account is blocked. Please contact admin.", HttpStatus.FORBIDDEN),
    AUTH_ACCOUNT_LOCKED(4030,"Account temporarily locked due to failed attempts.", HttpStatus.TOO_MANY_REQUESTS),
    AUTH_CAPTCHA_REQUIRED(4031,"Captcha verification required. Please solve the challenge.", HttpStatus.BAD_REQUEST),
    AUTH_TOKEN_REPLAY_DETECTED(4032, "Token reuse detected - possible attack. Please log in again.", HttpStatus.UNAUTHORIZED),
    AUTH_USER_DELETED(4020, "User account has been deleted", HttpStatus.GONE),
    AUTH_PERMISSION_ALREADY_EXISTS(4033, "Permission already exists", HttpStatus.CONFLICT),
    AUTH_ROLE_NOT_FOUND(4034, "Role not found", HttpStatus.NOT_FOUND),
    AUTH_ROLE_ALREADY_EXISTS(4035, "Role already exists", HttpStatus.CONFLICT),
    OTP_STILL_ACTIVE(4026, "OTP is still active", HttpStatus.TOO_MANY_REQUESTS),
    OTP_SEND_BLOCKED(4027,"Too many attempts. Please wait before requesting a new OTP.", HttpStatus.TOO_MANY_REQUESTS),

    // OTP
    OTP_INVALID(4028, "OTP is invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(4021, "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_EXCEEDED(4022, "Too many OTP attempts", HttpStatus.TOO_MANY_REQUESTS),
    OTP_ALREADY_USED(4023, "OTP has already been used", HttpStatus.BAD_REQUEST),
    OTP_RESEND_TOO_SOON(4024, "Please wait before requesting a new OTP", HttpStatus.TOO_MANY_REQUESTS),
    OTP_DAILY_LIMIT_EXCEEDED(4025, "Daily OTP limit exceeded. Try again tomorrow.", HttpStatus.TOO_MANY_REQUESTS),

    // Rate Limiting
    RATE_LIMIT_EXCEEDED(4290, "Too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS),

    // User
    USER_NOT_FOUND(5003, "User not found", HttpStatus.NOT_FOUND),
    USER_PROFILE_NOT_FOUND(5001, "User profile not found", HttpStatus.NOT_FOUND),
    USER_INVALID_DATE_OF_BIRTH(5002, "Date of birth cannot be in the future", HttpStatus.BAD_REQUEST),

    // Address
    ADDRESS_NOT_FOUND(5101, "Address not found", HttpStatus.NOT_FOUND),
    USER_ADDRESS_LIMIT_EXCEEDED(5102, "Maximum address limit exceeded", HttpStatus.BAD_REQUEST),

    // Catalog
    PRODUCT_NOT_FOUND(6001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_SLUG_ALREADY_EXISTS(6002, "Product with this slug already exists", HttpStatus.CONFLICT),
    PRODUCT_NOT_ACTIVE(6003, "Product is not active", HttpStatus.BAD_REQUEST),
    PRODUCT_INSUFFICIENT_STOCK(6004, "Insufficient stock", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(6011, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_SLUG_ALREADY_EXISTS(6012, "Category slug already exists", HttpStatus.CONFLICT),
    CATEGORY_HAS_CHILDREN(6013, "Cannot delete category with subcategories", HttpStatus.BAD_REQUEST),

    // Order
    ORDER_NOT_FOUND(7001, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_CANNOT_BE_CANCELLED(7002, "Order cannot be cancelled in its current state", HttpStatus.BAD_REQUEST),
    ORDER_INVALID_STATUS_TRANSITION(7003, "Invalid order status transition", HttpStatus.BAD_REQUEST),
    ORDER_NOT_PAID(7004, "Order payment has not been completed", HttpStatus.BAD_REQUEST),
    CART_EMPTY(7005, "Cart is empty", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(7006, "Cart item not found", HttpStatus.NOT_FOUND),
    ORDER_FORBIDDEN(7007, "You do not have permission to access this order", HttpStatus.FORBIDDEN),

    // Payment
    PAYMENT_NOT_FOUND(8001, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_INVALID_STATUS_TRANSITION(8002, "Invalid payment status transition", HttpStatus.BAD_REQUEST),
    PAYMENT_IDEMPOTENCY_CONFLICT(8003, "A payment with this idempotency key already exists", HttpStatus.CONFLICT),
    PAYMENT_ALREADY_PAID(8004, "Payment has already been completed", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_REFUNDABLE(8005, "Payment cannot be refunded in its current state", HttpStatus.BAD_REQUEST),
    PAYMENT_REFUND_AMOUNT_INVALID(8006, "Refund amount is invalid", HttpStatus.BAD_REQUEST),
    PAYMENT_REFUND_NOT_FOUND(8007, "Refund not found", HttpStatus.NOT_FOUND),
    PAYMENT_REFUND_INVALID_STATUS_TRANSITION(8008, "Invalid refund status transition", HttpStatus.BAD_REQUEST),
    PAYMENT_FORBIDDEN(8009, "You do not have permission to access this payment", HttpStatus.FORBIDDEN),
    PAYMENT_GATEWAY_ERROR(8010, "Payment gateway error", HttpStatus.BAD_GATEWAY),
    PAYMENT_METHOD_NOT_SUPPORTED(8011, "Payment method is not supported", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH(8012, "Payment amount does not match order total", HttpStatus.BAD_REQUEST),

    // Voucher
    VOUCHER_NOT_FOUND(9001, "Voucher not found", HttpStatus.NOT_FOUND),
    VOUCHER_INVALID_STATUS_TRANSITION(9002, "Invalid voucher status transition", HttpStatus.BAD_REQUEST),
    VOUCHER_EXPIRED(9003, "Voucher has expired", HttpStatus.BAD_REQUEST),
    VOUCHER_ALREADY_USED(9004, "Voucher has already been used", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_ACTIVE(9005, "Voucher is not active", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_ORDER_NOT_MET(9006, "Minimum order amount not met for this voucher", HttpStatus.BAD_REQUEST),
    VOUCHER_USAGE_LIMIT_EXCEEDED(9007, "Voucher usage limit exceeded", HttpStatus.BAD_REQUEST),
    VOUCHER_USER_LIMIT_EXCEEDED(9008, "You have already used this voucher the maximum number of times", HttpStatus.BAD_REQUEST),
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


    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getHttpStatus() { return httpStatus; }

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
