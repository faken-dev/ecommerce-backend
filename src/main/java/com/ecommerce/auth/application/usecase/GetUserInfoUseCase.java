package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.dto.UserResponse;
import com.ecommerce.auth.application.mapper.AuthApplicationMapper;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserInfoUseCase {

    private final UserRepository userRepository;
    private final AuthApplicationMapper mapper;

    @Transactional(readOnly = true)
    public UserResponse execute(UUID userId) {
        return userRepository.findById(userId)
                .map(mapper::toUserResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
    }
}
