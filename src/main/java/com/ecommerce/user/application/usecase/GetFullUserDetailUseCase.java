package com.ecommerce.user.application.usecase;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.user.application.dto.UserDetailAdminResponse;
import com.ecommerce.user.application.mapper.UserMapper;
import com.ecommerce.user.domain.entity.Address;
import com.ecommerce.user.domain.entity.User;
import com.ecommerce.user.domain.entity.UserProfile;
import com.ecommerce.user.domain.repository.AddressRepository;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import com.ecommerce.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFullUserDetailUseCase {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDetailAdminResponse execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder()
                        .userId(userId)
                        .fullName(user.getFullName())
                        .profilePictureUrl(user.getProfilePictureUrl())
                        .build());

        List<Address> addresses = addressRepository.findByUserIdOrderByDefault(userId);

        return userMapper.toDetailAdminResponse(user, profile, addresses);
    }
}
