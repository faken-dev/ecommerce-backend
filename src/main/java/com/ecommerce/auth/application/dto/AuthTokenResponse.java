package com.ecommerce.auth.application.dto;


public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long accessExpiresIn,    // seconds
        String tokenType
) {
    public static AuthTokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new AuthTokenResponse(accessToken, refreshToken, expiresIn, "Bearer");
    }
}