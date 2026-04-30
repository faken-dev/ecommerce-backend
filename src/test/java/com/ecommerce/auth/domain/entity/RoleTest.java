package com.ecommerce.auth.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

@DisplayName("Role Entity — Domain Tests")
class RoleTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create role with name and description")
        void createsCorrectly() {
            Role role = Role.create("ADMIN", "System administrator");

            assertThat(role.getName()).isEqualTo("ADMIN");
            assertThat(role.getDescription()).isEqualTo("System administrator");
            assertThat(role.getPermissions()).isEmpty();
            assertThat(role.getId()).isNotNull();
        }
    }

    // ── Permissions ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Permissions management")
    class Permissions {

        @Test
        @DisplayName("addPermission should add permission to set")
        void addsPermission() {
            Role role = Role.create("BUYER", "Standard buyer");
            Permission perm = Permission.create("catalog:read", "Read products");

            role.addPermission(perm);

            assertThat(role.getPermissions()).hasSize(1);
            assertThat(role.hasPermission("catalog:read")).isTrue();
        }

        @Test
        @DisplayName("removePermission should remove permission from set")
        void removesPermission() {
            Role role = Role.create("BUYER", "Standard buyer");
            Permission perm = Permission.create("catalog:read", "Read");
            role.addPermission(perm);
            role.removePermission(perm);

            assertThat(role.getPermissions()).isEmpty();
            assertThat(role.hasPermission("catalog:read")).isFalse();
        }

        @Test
        @DisplayName("hasPermission should return true for existing permission")
        void hasPermission() {
            Role role = Role.create("EDITOR", "Content editor");
            Permission readPerm = Permission.create("catalog:read", "Read products");
            role.addPermission(readPerm);
            assertThat(role.hasPermission("catalog:read")).isTrue();
            assertThat(role.hasPermission("nonexistent")).isFalse();
        }
    }

    // ── Equality ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals / hashCode")
    class Equality {

        @Test
        @DisplayName("roles with same ID are equal")
        void sameIdEqual() {
            UUID id =  UUID.randomUUID();
            Role r1 = Role.builder().id(id).name("A").description("d").permissions(Set.of()).build();
            Role r2 = Role.builder().id(id).name("B").description("e").permissions(Set.of()).build();

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }
    }
}