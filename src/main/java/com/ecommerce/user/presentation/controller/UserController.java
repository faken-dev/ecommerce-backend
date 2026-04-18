package com.ecommerce.user.presentation.controller;

import com.ecommerce.user.application.command.UpdateAvatarCommand;
import com.ecommerce.user.application.command.UpdateProfileCommand;
import com.ecommerce.user.application.dto.ProfileResponse;
import com.ecommerce.user.application.usecase.*;
import com.ecommerce.user.presentation.dto.request.UpdateAvatarRequest;
import com.ecommerce.user.presentation.dto.request.UpdateProfileRequest;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final UpdateAvatarUseCase updateAvatarUseCase;

    @GetMapping
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(getProfileUseCase.execute(userId)));
    }

    @PutMapping
    @Operation(summary = "Update current user's profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateProfileRequest req) {
        ProfileResponse profile = updateProfileUseCase.execute(userId,
                new UpdateProfileCommand(
                        req.fullName(),
                        req.bio(),
                        req.dateOfBirth(),
                        req.gender(),
                        req.profilePictureUrl()));
        return ResponseEntity.ok(ApiResponse.ok(profile, "Profile updated successfully"));
    }

    @PutMapping("/avatar")
    @Operation(summary = "Update avatar URL directly")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateAvatar(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateAvatarRequest req) {
        ProfileResponse profile = updateAvatarUseCase.execute(userId,
                new UpdateAvatarCommand(req.avatarUrl()));
        return ResponseEntity.ok(ApiResponse.ok(profile, "Avatar updated successfully"));
    }
}