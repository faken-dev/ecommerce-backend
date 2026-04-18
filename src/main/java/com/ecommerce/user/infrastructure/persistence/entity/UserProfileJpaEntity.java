package com.ecommerce.user.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserProfileJpaEntity extends AuditableJpaEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "default_address_id")
    private UUID defaultAddressId;

    public enum Gender { MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY }
}