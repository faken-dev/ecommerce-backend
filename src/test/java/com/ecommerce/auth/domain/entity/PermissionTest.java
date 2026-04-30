package com.ecommerce.auth.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Permission Entity — Domain Tests")
class PermissionTest {

    @Nested
    @DisplayName("Basic construction")
    class Construction {

        @Test
        @DisplayName("should store name and description")
        void storesFields() {
            Permission perm = Permission.create("catalog:write", "Create and update products");

            assertThat(perm.getName()).isEqualTo("catalog:write");
            assertThat(perm.getDescription()).isEqualTo("Create and update products");
        }
    }

    @Nested
    @DisplayName("getName()")
    class GetName {

        @Test
        @DisplayName("should return the permission name")
        void returnsName() {
            Permission perm = Permission.create("user:read", "Read users");

            assertThat(perm.getName()).isEqualTo("user:read");
        }
    }
}