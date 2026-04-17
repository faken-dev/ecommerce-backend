package com.ecommerce.auth.application.handler;

import com.ecommerce.auth.application.dto.AuthTokenResponse;
import com.ecommerce.auth.application.dto.VerifyOtpResponse;
import com.ecommerce.auth.application.mapper.AuthApplicationMapper;
import com.ecommerce.auth.application.service.TokenService;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles OTP verification for LOGIN and EMAIL_VERIFICATION purposes.
 * Generates and returns authentication tokens to the client.
 */
@Component
@RequiredArgsConstructor
public class AuthTokenOtpHandler implements OtpVerificationHandler {

    private final TokenService tokenService;
    private final AuthApplicationMapper mapper;

    @Override
    public boolean supports(OtpToken.Purpose purpose) {
        return purpose == OtpToken.Purpose.LOGIN
            || purpose == OtpToken.Purpose.EMAIL_VERIFICATION;
    }

    @Override
    public VerifyOtpResponse handle(User user) {
        AuthTokenResponse tokens = tokenService.generateTokens(user, "OTP_AUTH", "system");
        return new VerifyOtpResponse(
                mapper.toUserResponse(user),
                tokens,
                "Authentication successful");
    }
}
