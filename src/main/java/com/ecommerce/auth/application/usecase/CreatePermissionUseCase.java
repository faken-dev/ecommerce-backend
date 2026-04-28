package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.CreatePermissionCommand;
import com.ecommerce.auth.application.dto.PermissionResponse;
import com.ecommerce.auth.domain.entity.Permission;
import com.ecommerce.auth.domain.repository.PermissionRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatePermissionUseCase {

    private final PermissionRepository repository;

    public PermissionResponse execute(CreatePermissionCommand cmd) {
        if (repository.existsByName(cmd.name())) {
            throw new BusinessException(ErrorCode.AUTH_PERMISSION_ALREADY_EXISTS);
        }

        Permission permission = Permission.create(cmd.name(), cmd.description());
        Permission saved = repository.save(permission);

        return new PermissionResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
