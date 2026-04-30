package com.ecommerce.user.application.command;

import java.util.Set;

public record CreateRoleCommand(
    String name,
    String description,
    Set<String> permissions
) {}
