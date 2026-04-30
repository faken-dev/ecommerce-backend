package com.ecommerce.user.presentation.controller;

import java.util.UUID;


import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.user.application.command.CreateUserCommand;
import com.ecommerce.user.application.command.UpdateUserCommand;
import com.ecommerce.user.application.dto.UserAdminResponse;
import com.ecommerce.user.application.dto.UserDetailAdminResponse;
import com.ecommerce.user.application.usecase.CreateUserUseCase;
import com.ecommerce.user.application.usecase.DeleteUserUseCase;
import com.ecommerce.user.application.usecase.GetFullUserDetailUseCase;
import com.ecommerce.user.application.usecase.SearchUsersUseCase;
import com.ecommerce.user.application.usecase.UpdateUserUseCase;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/admin")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Endpoints for administrators to manage users")
public class AdminUserController {

    private final SearchUsersUseCase searchUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetFullUserDetailUseCase getFullUserDetailUseCase;

    @Operation(summary = "Search users with filters and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<Iterable<UserAdminResponse>>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(searchUsersUseCase.execute(role, search, pageable)));
    }

    @Operation(summary = "Get user profile (Admin)")
    @GetMapping("/{userId}/profile")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<UserDetailAdminResponse>> getUserProfile(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(getFullUserDetailUseCase.execute(userId)));
    }

    @Operation(summary = "Create a new user (Admin)")
    @PostMapping
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<ApiResponse<UserAdminResponse>> createUser(
            @Valid @RequestBody CreateUserCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(createUserUseCase.execute(cmd), "User created successfully"));
    }

    @Operation(summary = "Update user details (Admin)")
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserCommand cmd) {
        return ResponseEntity.ok(ApiResponse.ok(updateUserUseCase.execute(userId, cmd), "User updated successfully"));
    }

    @Operation(summary = "Delete user (Admin)")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID userId) {
        deleteUserUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "User deleted successfully"));
    }
}


