package com.ecommerce.order.infrastructure.adapter;

import com.ecommerce.order.application.port.UserQueryPort;
import com.ecommerce.user.infrastructure.persistence.entity.UserAccountJpaEntity;
import com.ecommerce.user.infrastructure.persistence.repository.UserAccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserAccountJpaRepository userRepository;

    @Override
    public String getUserFullName(UUID userId) {
        return userRepository.findById(userId)
                .map(UserAccountJpaEntity::getFullName)
                .orElse("N/A");
    }
}
