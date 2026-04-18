package com.ecommerce.user.application.usecase;

import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.user.application.command.UpdateProfileCommand;
import com.ecommerce.user.application.dto.ProfileResponse;
import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateProfileUseCase {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;   // auth module — keeps fullName in sync

    @Transactional
    public ProfileResponse execute(UUID userId, UpdateProfileCommand command) {
        if (command.dateOfBirth() != null && command.dateOfBirth().isAfter(java.time.LocalDate.now())) {
            throw new BusinessException(ErrorCode.USER_INVALID_DATE_OF_BIRTH);
        }

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND));

        profile.updateProfile(
                command.fullName(),
                command.profilePictureUrl(),
                command.bio(),
                command.dateOfBirth(),
                command.gender(),
                profile.getDefaultAddressId()
        );

        UserProfile saved = userProfileRepository.save(profile);

        // UserProfile.fullName is the authoritative source — propagate to auth_users
        userRepository.updateFullName(userId, saved.getFullName());

        return ProfileResponse.from(saved);
    }
}
