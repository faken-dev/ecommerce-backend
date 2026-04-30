package com.ecommerce.order.application.port;

import java.util.UUID;

public interface ShippingQueryPort {
    String getFullAddress(UUID addressId);
}
