package com.ecommerce.shared.response;

import com.ecommerce.shared.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int code;
    private String message;
    private T data;
    private PageMetadata page;
    private Object errors;
    @Builder.Default
    private Instant timestamp = Instant.now();

    // Success Responses
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).code(200).message("Success").data(data).build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder().success(true).code(200).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ok(data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder().success(true).code(200).message(message).build();
    }

    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder().success(true).code(200).message("Success").build();
    }

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
        return ApiResponse.<T>builder().success(false).code(errorCode.getCode()).message(errorCode.getMessage()).build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, Object errors) {
        return ApiResponse.<T>builder().success(false).code(errorCode.getCode()).message(errorCode.getMessage()).errors(errors).build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return ApiResponse.<T>builder().success(false).code(errorCode.getCode()).message(message).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).code(0).message(message).build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata {
        private int size;
        private int number;
        private long totalElements;
        private int totalPages;
    }
}
