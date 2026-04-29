package com.ecommerce.user.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Address extends AuditableEntity {
    private UUID userId;
    private String recipientName;
    private String phoneNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;
    private boolean isDefault;

    public static Address create(UUID userId, String recipientName, String phoneNumber,
                                 String street, String ward, String district, String city) {
        Instant now = Instant.now();
        return Address.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .userId(userId)
                .recipientName(recipientName)
                .phoneNumber(phoneNumber)
                .street(street)
                .ward(ward)
                .district(district)
                .city(city)
                .country("Vietnam")
                .isDefault(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void update(String recipientName, String phoneNumber, String street,
                       String ward, String district, String city, String country) {
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.country = country;
        this.setUpdatedAt(Instant.now());
    }

    public void markAsDefault() {
        this.isDefault = true;
        this.setUpdatedAt(Instant.now());
    }

    public void unmarkAsDefault() {
        this.isDefault = false;
        this.setUpdatedAt(Instant.now());
    }

    public String getFullAddress() {
        return String.format("%s, %s, %s, %s, %s", street, ward, district, city, country);
    }
}