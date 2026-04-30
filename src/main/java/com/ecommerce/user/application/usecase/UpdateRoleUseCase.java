package com.ecommerce.user.application.usecase;

import com.ecommerce.auth.infrastructure.persistence.entity.PermissionJpaEntity;
import com.ecommerce.auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.ecommerce.auth.infrastructure.persistence.repository.PermissionJpaRepository;
import com.ecommerce.auth.infrastructure.persistence.repository.RoleJpaRepository;
import com.ecommerce.user.application.command.UpdateRoleCommand;
import com.ecommerce.user.application.dto.RoleResponse;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateRoleUseCase {

    private final RoleJpaRepository roleJpaRepository;
    private final PermissionJpaRepository permissionJpaRepository;

    @Transactional
    public RoleResponse execute(UUID roleId, UpdateRoleCommand cmd) {
        RoleJpaEntity role = roleJpaRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found"));

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
