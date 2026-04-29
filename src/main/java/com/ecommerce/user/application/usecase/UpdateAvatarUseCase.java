package com.ecommerce.user.application.usecase;

import com.ecommerce.user.application.command.UpdateAvatarCommand;
import com.ecommerce.user.application.dto.ProfileResponse;
import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import com.ecommerce.user.domain.event.UserAvatarUpdatedEvent;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateAvatarUseCase {

    private final UserProfileRepository userProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProfileResponse execute(UUID userId, UpdateAvatarCommand command) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND));

        profile.updateAvatar(command.avatarUrl());
        UserProfile saved = userProfileRepository.save(profile);
        
        // Sync with Auth system
        eventPublisher.publishEvent(new UserAvatarUpdatedEvent(userId, command.avatarUrl()));
        
        return ProfileResponse.from(saved);
    }
}
