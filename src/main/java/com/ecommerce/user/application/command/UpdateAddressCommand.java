package com.ecommerce.user.application.command;

public record UpdateAddressCommand(
        String recipientName,
        String recipientPhone,
        String addressLine,
        String ward,
        String district,
        String province
) {}
