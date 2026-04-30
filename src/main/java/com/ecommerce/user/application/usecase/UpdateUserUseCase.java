package com.ecommerce.user.application.usecase;

import com.ecommerce.user.application.command.UpdateUserCommand;
import com.ecommerce.user.application.dto.UserAdminResponse;
import com.ecommerce.user.application.mapper.UserMapper;
import com.ecommerce.user.domain.entity.User;
import com.ecommerce.user.domain.repository.UserRepository;
import com.ecommerce.user.infrastructure.persistence.repository.UserProfileJpaRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final UserProfileJpaRepository userProfileJpaRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserAdminResponse execute(UUID userId, UpdateUserCommand cmd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found: " + userId));

        // Use domain methods for business rules/updates
        user.updateBasicInfo(cmd.fullName(), cmd.phoneNumber(), cmd.profilePictureUrl());
        user.updateStatus(cmd.active(), cmd.emailVerified());
        
        if (cmd.roles() != null) {
            user.updateRoles(cmd.roles());
        }

        User updated = userRepository.save(user);

        // Profile sync (Infrastructure concern, keeping it here for now but ideally via domain event)
        userProfileJpaRepository.findByUserId(userId).ifPresent(profile -> {
            profile.setProfilePictureUrl(cmd.profilePictureUrl());
            userProfileJpaRepository.save(profile);
        });

        return userMapper.toAdminResponse(updated);
    }
}
