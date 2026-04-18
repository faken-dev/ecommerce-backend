package com.ecommerce.user.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AddressJpaEntity extends AuditableJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "recipient_name", nullable = false, length = 255)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "address_line", nullable = false, length = 500)
    private String addressLine;

    @Column(name = "ward", nullable = false, length = 255)
    private String ward;

    @Column(name = "district", nullable = false, length = 255)
    private String district;

    @Column(name = "province", nullable = false, length = 255)
    private String province;

    @Column(name = "default_address", nullable = false)
    @Builder.Default
    private boolean defaultAddress = false;
}
