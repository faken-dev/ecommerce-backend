package com.ecommerce.catalog.application.command;

import java.util.UUID;

public record ActivateProductCommand(UUID productId) {}