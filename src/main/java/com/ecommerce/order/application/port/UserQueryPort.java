package com.ecommerce.order.application.port;

import java.util.UUID;

public interface UserQueryPort {
    String getUserFullName(UUID userId);
}
