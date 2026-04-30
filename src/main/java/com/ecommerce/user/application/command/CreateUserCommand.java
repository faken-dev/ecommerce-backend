package com.ecommerce.user.application.command;

import java.util.Set;

public record CreateUserCommand(
    String email,
    String password,
    String fullName,
    String phoneNumber,
    boolean active,
    boolean emailVerified,
    Set<String> roles
) {}
