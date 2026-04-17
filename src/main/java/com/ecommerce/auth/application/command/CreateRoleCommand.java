package com.ecommerce.auth.application.command;

public record CreateRoleCommand(
        String name,
        String description
) {}