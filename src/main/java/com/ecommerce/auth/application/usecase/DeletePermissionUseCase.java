package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.domain.repository.PermissionRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletePermissionUseCase {

    private final PermissionRepository repository;

    public void execute(UUID id) {
        if (repository.findById(id).isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found");
        }
        repository.deleteById(id);
    }
}