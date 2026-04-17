package com.ecommerce.shared.response;

import com.ecommerce.shared.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.Instant;


@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T data;
    private final PageMetadata page;
    private final Object errors;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    // Success Responses

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
    return ApiResponse.<T>builder()
            .success(true)
            .code(200)
            .message(message)
            .data(data)
            .build();
}

    /**
     * Spring Data Page
     */
    public static <T> ApiResponse<Iterable<T>> ok(Page<T> pageData) {
        return ApiResponse.<Iterable<T>>builder()
                .success(true)
                .code(200)
                .message("Success")
                .data(pageData.getContent())
                .page(PageMetadata.builder()
                        .size(pageData.getSize())
                        .number(pageData.getNumber())
                        .totalElements(pageData.getTotalElements())
                        .totalPages(pageData.getTotalPages())
                        .build())
                .build();
    }

    // Error Responses

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(errors)
                .build();
    }

    /** Puts custom message in the message field (e.g. custom BusinessException messages). */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(message)
                .errors(null)
                .build();
    }

    /** Convenience overload — uses code 0 for non-standard error messages. */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(0)
                .message(message)
                .build();
    }



    @Getter
    @Builder
    public static class PageMetadata {
        private final int size;
        private final int number;
        private final long totalElements;
        private final int totalPages;
    }
}