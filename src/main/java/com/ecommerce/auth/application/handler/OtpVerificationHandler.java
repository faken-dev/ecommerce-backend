package com.ecommerce.auth.application.handler;

import com.ecommerce.auth.application.dto.VerifyOtpResponse;
import com.ecommerce.auth.domain.entity.OtpToken;
import com.ecommerce.auth.domain.entity.User;

public interface OtpVerificationHandler {
    boolean supports(OtpToken.Purpose purpose);
    VerifyOtpResponse handle(User user);
}