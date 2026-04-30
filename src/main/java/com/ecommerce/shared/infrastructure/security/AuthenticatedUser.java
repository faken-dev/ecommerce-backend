package com.ecommerce.shared.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AuthenticatedUser {
    private final UUID id;
    private final String email;
    private final String fullName;

    @Override
    public String toString() {
        return id.toString();
    }
}
