package com.ecommerce.auth.domain.valueobject;

import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PhoneNumber Value Object")
class PhoneNumberTest {

    // ── Construction ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("constructor")
    class Construction {

        @Test
        @DisplayName("should accept valid E.164 format number")
        void acceptsValid() {
            PhoneNumber phone = new PhoneNumber("+84909123456");
            assertThat(phone.value()).isEqualTo("+84909123456");
        }

        @Test
        @DisplayName("should trim whitespace")
        void trims() {
            PhoneNumber phone = new PhoneNumber("  +84909123456  ");
            assertThat(phone.value()).isEqualTo("+84909123456");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "+123", "84909123456", "phone-number", "+84909abcdef"})
        @DisplayName("should throw for invalid format")
        void rejectsInvalid(String input) {
            assertThatThrownBy(() -> new PhoneNumber(input))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("should throw when null")
        void rejectsNull() {
            assertThatThrownBy(() -> new PhoneNumber(null))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    // ── of() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("of()")
    class Of {

        @Test
        @DisplayName("should return null for blank input")
        void returnsNullForBlank() {
            assertThat(PhoneNumber.of("")).isNull();
            assertThat(PhoneNumber.of("   ")).isNull();
            assertThat(PhoneNumber.of(null)).isNull();
        }

        @Test
        @DisplayName("should return PhoneNumber for valid input")
        void returnsPhoneNumber() {
            PhoneNumber phone = PhoneNumber.of("+84909123456");
            assertThat(phone.value()).isEqualTo("+84909123456");
        }
    }

    // ── toString ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toString()")
    class ToString {

        @Test
        @DisplayName("should return the value")
        void returnsValue() {
            PhoneNumber phone = new PhoneNumber("+84909123456");
            assertThat(phone.toString()).isEqualTo("+84909123456");
        }
    }
}