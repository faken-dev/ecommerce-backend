package com.ecommerce.user.application.usecase;

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
public class GetProfileUseCase {

    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public ProfileResponse execute(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND));
        return ProfileResponse.from(profile);
    }
}
