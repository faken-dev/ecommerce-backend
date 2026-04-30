package com.ecommerce.auth.domain.valueobject;

import com.ecommerce.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email Value Object")
class EmailTest {

    // ── Construction ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("constructor / of()")
    class Construction {

        @Test
        @DisplayName("should accept valid email")
        void acceptsValid() {
            Email email = new Email("user@example.com");
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("should trim whitespace")
        void trims() {
            Email email = new Email("  user@example.com  ");
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "not-an-email", "@example.com", "user@", "user name@example.com"})
        @DisplayName("should throw for invalid email formats")
        void rejectsInvalid(String input) {
            assertThatThrownBy(() -> new Email(input))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ── equals / hashCode ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals / hashCode")
    class Equality {

        @Test
        @DisplayName("same value should be equal")
        void sameValueEqual() {
            Email e1 = new Email("test@example.com");
            Email e2 = new Email("test@example.com");
            assertThat(e1).isEqualTo(e2);
            assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        }

        @Test
        @DisplayName("different values should not be equal")
        void differentNotEqual() {
            Email e1 = new Email("test@example.com");
            Email e2 = new Email("other@example.com");
            assertThat(e1).isNotEqualTo(e2);
        }

        @Test
        @DisplayName("should not equal non-Email objects")
        void notEqualToString() {
            Email email = new Email("test@example.com");
            assertThat(email).isNotEqualTo("test@example.com");
        }
    }
}