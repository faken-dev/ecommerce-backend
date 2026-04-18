package com.ecommerce.auth.presentation.controller;

import com.ecommerce.auth.application.command.*;
import com.ecommerce.auth.application.dto.*;
import com.ecommerce.auth.application.service.TokenService;
import com.ecommerce.auth.application.usecase.*;
import com.ecommerce.auth.presentation.dto.request.*;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication & Authorization endpoints")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final SendOtpUseCase sendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final TokenService tokenService;

    // ── Auth Endpoints ─────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = registerUseCase.execute(
                new RegisterCommand(
                        request.email(),
                        request.password(),
                        request.fullName(),
                        request.phoneNumber()
                ));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Registration successful"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse response = loginUseCase.execute(
                new LoginCommand(
                        request.email(),
                        request.password(),
                        httpRequest.getHeader("User-Agent"),
                        httpRequest.getRemoteAddr(),
                        request.captchaToken()
                ));

        return ResponseEntity.ok(ApiResponse.ok(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        AuthTokenResponse response = refreshTokenUseCase.execute(
                new RefreshTokenCommand(
                        request.refreshToken(),
                        httpRequest.getHeader("User-Agent"),
                        httpRequest.getRemoteAddr()
                ));

        return ResponseEntity.ok(ApiResponse.ok(response, "Token refreshed"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout from current device",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        String accessToken = extractBearerToken(httpRequest);
        long remainingMs = tokenService.getRemainingValidityMs(accessToken);

        logoutUseCase.execute(request.refreshToken(), accessToken, remainingMs);

        return ResponseEntity.ok(ApiResponse.ok(null, "Logged out successfully"));
    }

    @PostMapping("/logout/all")
    @Operation(summary = "Logout from all devices",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpRequest) {

        String accessToken = extractBearerToken(httpRequest);
        long remainingMs = tokenService.getRemainingValidityMs(accessToken);

        logoutUseCase.executeAllDevices(userId, accessToken, remainingMs);

        return ResponseEntity.ok(ApiResponse.ok(null, "Logged out from all devices"));
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP via Email / SMS / WhatsApp")
    //  SecurityConfig đã declare endpoint này là PUBLIC
    // → annotation này chỉ để Swagger hiển thị, không enforce auth
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {

        sendOtpUseCase.execute(
                new SendOtpCommand(
                        request.email(),
                        request.channel(),
                        request.purpose()
                )
        );

        return ResponseEntity.ok(ApiResponse.ok(null, "OTP sent successfully"));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP")
    // SecurityConfig đã declare endpoint này là PUBLIC
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtpAuthenticated(
            @Valid @RequestBody VerifyOtpRequest request) {

        var response = verifyOtpUseCase.execute(
                new VerifyOtpCommand(request.email(), request.code(), request.purpose()));

        return ResponseEntity.ok(ApiResponse.ok(response, "OTP verified successfully"));
    }

    @PostMapping("/password/forgot")
    @Operation(summary = "Request password reset OTP")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        forgotPasswordUseCase.execute(new ForgotPasswordCommand(request.email()));

        // Luôn trả 200 dù email có tồn tại hay không — tránh account enumeration
        return ResponseEntity.ok(ApiResponse.ok(null,
                "If the email exists, a reset code has been sent"));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        resetPasswordUseCase.execute(
                new ResetPasswordCommand(
                        request.email(),
                        request.otpCode(),
                        request.newPassword()
                ));

        return ResponseEntity.ok(ApiResponse.ok(null, "Password reset successfully"));
    }

    // ── Helpers ───────────────────────────────────────────────────────

    /**
     * Extract Bearer token từ Authorization header.
     * @return raw token, hoặc empty string nếu không có
     */
    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return extractFromHeader(header);
    }

    /**
     * Extract Bearer token từ String header value.
     */
    private String extractFromHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return "";
    }
}
