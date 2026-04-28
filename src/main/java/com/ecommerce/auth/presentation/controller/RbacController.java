package com.ecommerce.auth.presentation.controller;

import com.ecommerce.auth.application.command.AssignPermissionsCommand;
import com.ecommerce.auth.application.command.CreatePermissionCommand;
import com.ecommerce.auth.application.command.CreateRoleCommand;
import com.ecommerce.auth.application.dto.PermissionResponse;
import com.ecommerce.auth.application.dto.RoleResponse;
import com.ecommerce.auth.application.usecase.CreatePermissionUseCase;
import com.ecommerce.auth.application.usecase.CreateRoleUseCase;
import com.ecommerce.auth.application.usecase.DeletePermissionUseCase;
import com.ecommerce.auth.application.usecase.GetPermissionUseCase;
import com.ecommerce.auth.application.usecase.ManageRoleUseCase;
import com.ecommerce.auth.presentation.dto.request.AssignPermissionsRequest;
import com.ecommerce.auth.presentation.dto.request.CreatePermissionRequest;
import com.ecommerce.auth.presentation.dto.request.CreateRoleRequest;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/rbac")
@RequiredArgsConstructor
@Tag(name = "RBAC", description = "Role and Permission management")
public class RbacController {

    private final CreatePermissionUseCase createPermissionUseCase;
    private final GetPermissionUseCase getPermissionUseCase;
    private final DeletePermissionUseCase deletePermissionUseCase;
    private final CreateRoleUseCase createRoleUseCase;
    private final ManageRoleUseCase manageRoleUseCase;

    // ── Permission Endpoints ─────────────────────────────────────────────────

    @PostMapping("/permissions")
    @Operation(summary = "Create a new permission")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request,
            @AuthenticationPrincipal UUID adminId) {

        PermissionResponse response = createPermissionUseCase.execute(
                new CreatePermissionCommand(request.name(), request.description()));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Permission created"));
    }

    @GetMapping("/permissions")
    @Operation(summary = "List all permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> response = getPermissionUseCase.executeAll();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/permissions/{id}")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermission(@PathVariable UUID id) {
        PermissionResponse response = getPermissionUseCase.executeById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/permissions/{id}")
    @Operation(summary = "Delete a permission")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable UUID id) {
        deletePermissionUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Permission deleted"));
    }

    // ── Role Endpoints ──────────────────────────────────────────────────────

    @PostMapping("/roles")
    @Operation(summary = "Create a new role")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request,
            @AuthenticationPrincipal UUID adminId) {

        RoleResponse response = createRoleUseCase.execute(
                new CreateRoleCommand(request.name(), request.description()), adminId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Role created"));
    }

    @GetMapping("/roles")
    @Operation(summary = "List all roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> response = manageRoleUseCase.getAllRoles();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/roles/{id}")
    @Operation(summary = "Get role by ID with permissions")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable UUID id) {
        RoleResponse response = manageRoleUseCase.getRole(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/roles/{roleId}/permissions")
    @Operation(summary = "Assign permissions to a role")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionsRequest request) {

        RoleResponse response = manageRoleUseCase.assignPermissions(
                roleId, new AssignPermissionsCommand(request.permissionIds()));

        return ResponseEntity.ok(ApiResponse.ok(response, "Permissions assigned"));
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Revoke a permission from a role")
    public ResponseEntity<ApiResponse<RoleResponse>> revokePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {

        RoleResponse response = manageRoleUseCase.revokePermission(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Permission revoked"));
    }

    @DeleteMapping("/roles/{id}")
    @Operation(summary = "Soft-delete a role")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID adminId) {

        manageRoleUseCase.deleteRole(id, adminId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Role deleted"));
    }
}
