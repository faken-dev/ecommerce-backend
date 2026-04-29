package com.ecommerce.shared.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Otp otp = new Otp();

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank
        private String secret;
        
        @Positive
        private long accessExpirationMs;
        
        @Positive
        private long refreshExpirationMs;
    }

    @Getter
    @Setter
    public static class Otp {
        @Positive
        private int expiryMinutes;

        @Positive
        private int length;

        @Positive
        private int maxAttempts;

        @NotBlank(message = "OTP HMAC secret must be configured via OTP_HMAC_SECRET env var")
        private String hmacSecret;
    }
}
