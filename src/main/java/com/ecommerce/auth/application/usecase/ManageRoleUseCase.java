package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.AssignPermissionsCommand;
import com.ecommerce.auth.application.dto.PermissionResponse;
import com.ecommerce.auth.application.dto.RoleResponse;
import com.ecommerce.auth.domain.entity.Permission;
import com.ecommerce.auth.domain.entity.Role;
import com.ecommerce.auth.domain.repository.PermissionRepository;
import com.ecommerce.auth.domain.repository.RoleRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageRoleUseCase {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public RoleResponse getRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_ROLE_NOT_FOUND));
        return toResponse(role);
    }

    @Transactional
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RoleResponse assignPermissions(UUID roleId, AssignPermissionsCommand cmd) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_ROLE_NOT_FOUND));

        List<Permission> permissions = permissionRepository.findByIdIn(cmd.permissionIds());
        if (permissions.size() != cmd.permissionIds().size()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Some permissions not found");
        }

        for (Permission p : permissions) {
            role.addPermission(p);
        }
        Role saved = roleRepository.save(role);
        return toResponse(saved);
    }

    @Transactional
    public RoleResponse revokePermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_ROLE_NOT_FOUND));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found"));

        role.removePermission(permission);
        Role saved = roleRepository.save(role);
        return toResponse(saved);
    }

    @Transactional
    public void deleteRole(UUID roleId, UUID deletedBy) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_ROLE_NOT_FOUND));
        roleRepository.deleteById(roleId, deletedBy);
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
