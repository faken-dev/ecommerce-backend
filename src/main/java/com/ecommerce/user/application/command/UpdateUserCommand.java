package com.ecommerce.user.application.command;

import java.util.Set;

public record UpdateUserCommand(
    String fullName,
    String phoneNumber,
    String profilePictureUrl,
    Boolean active,
    Boolean emailVerified,
    Set<String> roles
) {}
