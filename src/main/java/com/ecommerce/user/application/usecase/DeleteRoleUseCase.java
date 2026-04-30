package com.ecommerce.user.application.usecase;

import com.ecommerce.auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.ecommerce.auth.infrastructure.persistence.repository.RoleJpaRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DeleteRoleUseCase {
    private static final Logger log = LoggerFactory.getLogger(DeleteRoleUseCase.class);

    private final RoleJpaRepository roleJpaRepository;

    @Transactional
    public void execute(UUID roleId) {
        log.info("Attempting to delete role with ID: {}", roleId);
        
        RoleJpaEntity role = roleJpaRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found"));
        
        // Protect system roles
        String name = role.getName();
        if (List.of("ADMIN", "BUYER", "SELLER").stream().anyMatch(s -> s.equalsIgnoreCase(name))) {
            log.warn("Attempted to delete protected system role: {}", name);
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Cannot delete system protected role: " + name);
        }

        roleJpaRepository.delete(role);
        log.info("Successfully deleted role with ID: {}", roleId);
    }
}
