package com.ecommerce.auth.application.command;

public record CreatePermissionCommand(
        String name,
        String description
) {}