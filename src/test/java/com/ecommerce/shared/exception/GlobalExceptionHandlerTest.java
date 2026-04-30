package com.ecommerce.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    // ── handleBusinessException ────────────────────────────────────────────────

    @Nested
    @DisplayName("handleBusinessException()")
    class BusinessExceptionHandling {

        @Test
        @DisplayName("should return 401 for AUTH_INVALID_CREDENTIALS")
        void authInvalid() {
            BusinessException ex = new BusinessException(
                    ErrorCode.AUTH_INVALID_CREDENTIALS, "Bad credentials");

            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleBusinessException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody().getCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS.getCode());
        }

        @Test
        @DisplayName("should return 404 for RESOURCE_NOT_FOUND")
        void notFound() {
            BusinessException ex = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);

            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleBusinessException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("should return 429 for AUTH_ACCOUNT_LOCKED")
        void locked() {
            BusinessException ex = new BusinessException(ErrorCode.AUTH_ACCOUNT_LOCKED,
                    "Try again in 30 seconds");

            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleBusinessException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(response.getBody().getCode())
                    .isEqualTo(ErrorCode.AUTH_ACCOUNT_LOCKED.getCode());
            // Custom message overrides the default ErrorCode message
            assertThat(response.getBody().getMessage()).contains("Try again");
        }

        @Test
        @DisplayName("should include custom message in response")
        void customMessage() {
            BusinessException ex = new BusinessException(
                    ErrorCode.VALIDATION_FAILED, "Field 'email' is required");

            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleBusinessException(ex, request);

            assertThat(response.getBody().getMessage()).contains("Field 'email'");
        }
    }

    // ── handleValidation ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("handleValidation()")
    class Validation {

        @Test
        @DisplayName("should return 400 with field errors map")
        void returnsFieldErrors() {
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError1 = new FieldError("cmd", "email", "must not be blank");
            FieldError fieldError2 = new FieldError("cmd", "password", "must be at least 8 characters");

            when(bindingResult.getFieldErrors())
                    .thenReturn(List.of(fieldError1, fieldError2));
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                    null, bindingResult);

            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleValidation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).isNotNull();
        }
    }

    // ── Security exceptions ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Security exceptions")
    class Security {

        @Test
        @DisplayName("should return 401 for AuthenticationException")
        void authException() {
            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleAuthentication(new AuthenticationException("Bad credentials") {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("should return 403 for AccessDeniedException")
        void accessDenied() {
            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleAccessDenied(new AccessDeniedException("denied"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    // ── Parameter exceptions ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Parameter type mismatches")
    class ParameterExceptions {

        @Test
        @DisplayName("should return 400 for MethodArgumentTypeMismatchException")
        void typeMismatch() {
            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleTypeMismatch(new MethodArgumentTypeMismatchException(
                            "uuid", String.class, "id", null, null));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should return 400 for MissingServletRequestParameterException")
        void missingParam() {
            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleTypeMismatch(new MissingServletRequestParameterException("page", "int"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ── Fallback handler ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("handleGeneral() — fallback")
    class Fallback {

        @Test
        @DisplayName("should return 500 with INTERNAL_SERVER_ERROR code")
        void returns500() {
            ResponseEntity<com.ecommerce.shared.response.ApiResponse<Void>> response =
                    handler.handleGeneral(new RuntimeException("unexpected"), request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getCode())
                    .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        }
    }
}
