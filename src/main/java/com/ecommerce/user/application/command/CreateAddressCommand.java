package com.ecommerce.user.application.command;

public record CreateAddressCommand(
        String recipientName,
        String recipientPhone,
        String addressLine,
        String ward,
        String district,
        String province,
        boolean isDefault
) {}