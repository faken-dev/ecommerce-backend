package com.ecommerce.user.application.usecase;

import com.ecommerce.user.infrastructure.persistence.entity.UserAccountJpaEntity;
import com.ecommerce.user.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.ecommerce.user.infrastructure.persistence.repository.UserAccountJpaRepository;
import com.ecommerce.user.infrastructure.persistence.repository.UserRoleAccountJpaRepository;
import com.ecommerce.user.application.command.CreateUserCommand;
import com.ecommerce.user.application.dto.UserAdminResponse;
import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.user.application.mapper.UserMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserAccountJpaRepository userJpaRepository;
    private final UserRoleAccountJpaRepository roleJpaRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserAdminResponse execute(CreateUserCommand cmd) {
        if (userJpaRepository.existsByEmail(cmd.email())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        UserAccountJpaEntity user = new UserAccountJpaEntity();
        user.setId(UuidCreator.getTimeOrderedEpoch());
        user.setEmail(cmd.email());
        user.setFullName(cmd.fullName());
        String phone = cmd.phoneNumber();
        user.setPhoneNumber(phone != null && !phone.isBlank() ? phone : null);
        user.setActive(cmd.active());
        user.setEmailVerified(cmd.emailVerified());

        if (cmd.roles() != null && !cmd.roles().isEmpty()) {
            Set<UserRoleJpaEntity> roles = cmd.roles().stream()
                    .map(name -> roleJpaRepository.findByName(name)
                            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found: " + name)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        } else {
            // Default role if none specified
            roleJpaRepository.findByName("BUYER").ifPresent(r -> user.getRoles().add(r));
        }

        UserAccountJpaEntity saved = userJpaRepository.save(user);

        // Create user profile
        UserProfile profile = UserProfile.createFromRegistration(saved.getId(), saved.getFullName());
        userProfileRepository.save(profile);

        return userMapper.toAdminResponse(userMapper.toDomain(saved));
    }
}
