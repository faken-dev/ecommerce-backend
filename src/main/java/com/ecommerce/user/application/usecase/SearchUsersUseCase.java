package com.ecommerce.user.application.usecase;

import com.ecommerce.user.application.dto.UserAdminResponse;
import com.ecommerce.user.application.mapper.UserMapper;
import com.ecommerce.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchUsersUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserAdminResponse> execute(String role, String search, Pageable pageable) {
        // Map empty strings to null for JPA query compatibility and strip ROLE_ prefix
        String roleFilter = (role != null && !role.isEmpty() && !role.equals("ALL")) 
                ? role.replace("ROLE_", "") 
                : null;
        String searchFilter = (search != null && !search.isEmpty()) ? search.toLowerCase() : "";

        return userRepository.searchUsers(roleFilter, searchFilter, pageable)
                .map(userMapper::toAdminResponse);
    }
}
