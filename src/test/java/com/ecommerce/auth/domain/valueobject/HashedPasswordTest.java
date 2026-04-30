package com.ecommerce.auth.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HashedPassword Value Object")
class HashedPasswordTest {

    @Nested
    @DisplayName("of()")
    class Of {

        @Test
        @DisplayName("should store hashed value")
        void storesValue() {
            HashedPassword hp = HashedPassword.of("hashed_value");
            assertThat(hp.value()).isEqualTo("hashed_value");
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class Equality {

        @Test
        @DisplayName("same hash should be equal")
        void sameEqual() {
            HashedPassword p1 = HashedPassword.of("secret");
            HashedPassword p2 = HashedPassword.of("secret");
            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("different hashes should not be equal")
        void differentNotEqual() {
            HashedPassword p1 = HashedPassword.of("secret1");
            HashedPassword p2 = HashedPassword.of("secret2");
            assertThat(p1).isNotEqualTo(p2);
        }
    }
}