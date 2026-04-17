package com.ecommerce.auth.application.command;

import java.util.List;
import java.util.UUID;

public record AssignPermissionsCommand(
        List<UUID> permissionIds
) {}
