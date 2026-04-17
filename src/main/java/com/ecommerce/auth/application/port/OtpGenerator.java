package com.ecommerce.auth.application.port;

public interface OtpGenerator {
    String generate(int length);
}