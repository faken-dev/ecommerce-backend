package com.ecommerce.shipping.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateShipmentCommand(
    UUID orderId,
    String recipientName,
    String phone,
    String street,
    String district,
    String city,
    String province,
    String country,
    BigDecimal weight,
    BigDecimal shippingFee
) {}
