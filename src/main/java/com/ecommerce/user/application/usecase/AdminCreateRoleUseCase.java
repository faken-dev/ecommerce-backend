package com.ecommerce.user.application.usecase;

import java.util.UUID;

import com.ecommerce.auth.infrastructure.persistence.entity.PermissionJpaEntity;
import com.ecommerce.auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.ecommerce.auth.infrastructure.persistence.repository.PermissionJpaRepository;
import com.ecommerce.auth.infrastructure.persistence.repository.RoleJpaRepository;
import com.ecommerce.user.application.command.CreateRoleCommand;
import com.ecommerce.user.application.dto.RoleResponse;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCreateRoleUseCase {

    private final RoleJpaRepository roleJpaRepository;
    private final PermissionJpaRepository permissionJpaRepository;

    @Transactional
    public RoleResponse execute(CreateRoleCommand cmd) {
        String roleName = cmd.name();
        
        if (roleJpaRepository.findByName(roleName).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_ROLE_ALREADY_EXISTS);
        }

        RoleJpaEntity role = new RoleJpaEntity();
        role.setId(UUID.randomUUID());
        role.setName(roleName);
        role.setDescription(cmd.description());

        if (cmd.permissions() != null) {
            Set<PermissionJpaEntity> permissions = cmd.permissions().stream()
                    .map(name -> permissionJpaRepository.findByName(name)
                            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found: " + name)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        RoleJpaEntity saved = roleJpaRepository.save(role);

        return RoleResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .permissions(saved.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toSet()))
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
