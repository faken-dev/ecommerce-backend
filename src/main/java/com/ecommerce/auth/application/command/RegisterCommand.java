package com.ecommerce.auth.application.command;

public record RegisterCommand(
        String email,
        String password,
        String fullName,
        String phoneNumber
) {}
