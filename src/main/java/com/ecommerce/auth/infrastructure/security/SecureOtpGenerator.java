package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.auth.application.port.OtpGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureOtpGenerator implements OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate(int length) {
        int bound = (int) Math.pow(10, length);
        int otp = RANDOM.nextInt(bound);
        // Pad with leading zeros if necessary
        return String.format("%0" + length + "d", otp);
    }
}
