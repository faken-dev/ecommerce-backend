package com.ecommerce.user.presentation.controller;

import java.util.UUID;

import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.user.application.command.CreateRoleCommand;
import com.ecommerce.user.application.command.UpdateRoleCommand;
import com.ecommerce.user.application.dto.RoleResponse;
import com.ecommerce.user.application.usecase.AdminCreateRoleUseCase;
import com.ecommerce.user.application.usecase.DeleteRoleUseCase;
import com.ecommerce.user.application.usecase.SearchRolesUseCase;
import com.ecommerce.user.application.usecase.UpdateRoleUseCase;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles/admin")
@Tag(name = "Admin Role Management", description = "Endpoints for managing system roles and permissions")
@RequiredArgsConstructor
public class AdminRoleController {

    private final SearchRolesUseCase searchRolesUseCase;
    private final AdminCreateRoleUseCase createRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;

    @Operation(summary = "List all roles")
    @GetMapping
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.ok(searchRolesUseCase.execute()));
    }

    @Operation(summary = "Create a new role")
    @PostMapping
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(createRoleUseCase.execute(cmd), "Role created successfully"));
    }

    @Operation(summary = "Update role details")
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleCommand cmd) {
        return ResponseEntity.ok(ApiResponse.ok(updateRoleUseCase.execute(roleId, cmd), "Role updated successfully"));
    }

    @Operation(summary = "Delete role")
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable UUID roleId) {
        deleteRoleUseCase.execute(roleId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Role deleted successfully"));
    }
}


