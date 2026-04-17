package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.dto.PermissionResponse;
import com.ecommerce.auth.domain.entity.Permission;
import com.ecommerce.auth.domain.repository.PermissionRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPermissionUseCase {

    private final PermissionRepository repository;

    public List<PermissionResponse> executeAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PermissionResponse executeById(UUID id) {
        Permission p = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found"));
        return toResponse(p);
    }

    private PermissionResponse toResponse(Permission p) {
        return new PermissionResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}