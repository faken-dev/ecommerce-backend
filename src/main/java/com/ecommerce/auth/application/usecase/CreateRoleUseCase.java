package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.CreateRoleCommand;
import com.ecommerce.auth.application.dto.PermissionResponse;
import com.ecommerce.auth.application.dto.RoleResponse;
import com.ecommerce.auth.domain.entity.Role;
import com.ecommerce.auth.domain.repository.RoleRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateRoleUseCase {

    private final RoleRepository roleRepository;

    @Transactional
    public RoleResponse execute(CreateRoleCommand cmd, UUID createdBy) {
        if (roleRepository.findByName(cmd.name()).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_ROLE_ALREADY_EXISTS);
        }

        Role role = Role.create(cmd.name(), cmd.description());
        role.setCreatedBy(createdBy);
        Role saved = roleRepository.save(role);

        return toResponse(saved);
    }

    private RoleResponse toResponse(Role r) {
        return new RoleResponse(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getPermissions().stream()
                        .map(p -> new PermissionResponse(p.getId(), p.getName(), p.getDescription(), p.getCreatedAt(), p.getUpdatedAt()))
                        .toList(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
