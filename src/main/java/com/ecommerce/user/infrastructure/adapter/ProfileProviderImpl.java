package com.ecommerce.user.infrastructure.adapter;

import com.ecommerce.auth.application.port.ProfileProvider;
import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileProviderImpl implements ProfileProvider {

    private final UserProfileRepository userProfileRepository;

    @Override
    public String getProfilePictureUrl(UUID userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getProfilePictureUrl)
                .orElse(null);
    }
}
