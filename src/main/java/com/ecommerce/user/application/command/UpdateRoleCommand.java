package com.ecommerce.user.application.command;

import java.util.Set;

public record UpdateRoleCommand(
    String description,
    Set<String> permissions
) {}
