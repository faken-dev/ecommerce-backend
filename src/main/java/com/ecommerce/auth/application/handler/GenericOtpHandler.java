package com.ecommerce.auth.application.handler;

import org.springframework.stereotype.Component;

import com.ecommerce.auth.application.dto.VerifyOtpResponse;
import com.ecommerce.auth.application.mapper.AuthApplicationMapper;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GenericOtpHandler implements OtpVerificationHandler {
    private final AuthApplicationMapper mapper;

    @Override
    public boolean supports(OtpToken.Purpose purpose) {
        return purpose == OtpToken.Purpose.PHONE_VERIFICATION || purpose == OtpToken.Purpose.PASSWORD_RESET;
    }

    @Override
    public VerifyOtpResponse handle(User user) {
        return new VerifyOtpResponse(mapper.toUserResponse(user), null, "OTP verified successfully");
    }
}
