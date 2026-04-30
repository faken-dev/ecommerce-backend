package com.ecommerce.user.application.command;

import java.util.Set;

public record UpdateUserCommand(
    String fullName,
    String phoneNumber,
    String profilePictureUrl,
    boolean active,
    boolean emailVerified,
    Set<String> roles
) {}
