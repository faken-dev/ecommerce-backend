package com.ecommerce.user.application.usecase;

import com.ecommerce.auth.infrastructure.persistence.repository.RoleJpaRepository;
import com.ecommerce.user.application.dto.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchRolesUseCase {

    private final RoleJpaRepository roleJpaRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> execute() {
        return roleJpaRepository.findAll().stream()
                .map(r -> RoleResponse.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .permissions(r.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toSet()))
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
