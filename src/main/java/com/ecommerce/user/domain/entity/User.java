package com.ecommerce.user.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Domain entity representing a User from the perspective of User/Profile management.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends AuditableEntity {
    private String email;
    private String fullName;
    private String phoneNumber;
    private String profilePictureUrl;
    private boolean active;
    private boolean emailVerified;
    private Set<String> roles;

    public void updateBasicInfo(String fullName, String phoneNumber, String profilePictureUrl) {
        this.fullName = fullName;
        this.phoneNumber = (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber : null;
        this.profilePictureUrl = profilePictureUrl;
        this.touchUpdate();
    }

    public void updateStatus(boolean active, boolean emailVerified) {
        this.active = active;
        this.emailVerified = emailVerified;
        this.touchUpdate();
    }

    public void updateRoles(Set<String> roles) {
        this.roles = roles;
        this.touchUpdate();
    }
}
