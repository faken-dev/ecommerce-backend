package com.ecommerce.shared.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse")
class ApiResponseTest {

    private static final com.ecommerce.shared.exception.ErrorCode ERR =
            com.ecommerce.shared.exception.ErrorCode.VALIDATION_FAILED;

    // ── ok() ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ok() factory")
    class OkFactory {

        @Test
        @DisplayName("should create success response with data")
        void withData() {
            ApiResponse<String> response = ApiResponse.ok("Hello");

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getCode()).isEqualTo(200);
            assertThat(response.getData()).isEqualTo("Hello");
            assertThat(response.getMessage()).isEqualTo("Success");
        }

        @Test
        @DisplayName("should allow custom message on success")
        void withCustomMessage() {
            ApiResponse<String> response = ApiResponse.ok("data", "Done!");

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo("data");
            assertThat(response.getMessage()).isEqualTo("Done!");
        }
    }

    // ── error() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("error() factory")
    class ErrorFactory {

        @Test
        @DisplayName("should create error response with code from ErrorCode enum")
        void basicError() {
            ApiResponse<Void> response = ApiResponse.error(ERR);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getCode()).isEqualTo(ERR.getCode());
            assertThat(response.getMessage()).isEqualTo(ERR.getMessage());
            assertThat(response.getData()).isNull();
        }

        @Test
        @DisplayName("should include field errors map in errors field")
        void fieldErrors() {
            ApiResponse<Void> response = ApiResponse.error(ERR,
                    java.util.Map.of("email", "must not be blank", "password", "too short"));

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getCode()).isEqualTo(ERR.getCode());
            assertThat(response.getErrors()).isNotNull();
        }

        @Test
        @DisplayName("should allow plain string error message")
        void stringMessage() {
            ApiResponse<Void> response = ApiResponse.error("Something went wrong");

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getCode()).isZero();
            assertThat(response.getMessage()).isEqualTo("Something went wrong");
        }
    }

    // ── JSON Serialization ───────────────────────────────────────────────────

    @Nested
    @DisplayName("JSON serialization")
    class JsonSerialization {

        @Test
        @DisplayName("should serialize success response with correct fields")
        void serializeSuccess() throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            ApiResponse<String> response = ApiResponse.ok("data", "Done");

            String json = mapper.writeValueAsString(response);

            assertThat(json).contains("\"success\":true");
            assertThat(json).contains("\"code\":200");
            assertThat(json).contains("\"data\":\"data\"");
            assertThat(json).contains("\"message\":\"Done\"");
        }

        @Test
        @DisplayName("should serialize error response with correct fields")
        void serializeError() throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            ApiResponse<Void> response = ApiResponse.error(ERR, "Field required");

            String json = mapper.writeValueAsString(response);

            assertThat(json).contains("\"success\":false");
            assertThat(json).contains("\"code\":" + ERR.getCode());
        }
    }
}
