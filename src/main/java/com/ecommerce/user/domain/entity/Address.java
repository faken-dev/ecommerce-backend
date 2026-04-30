package com.ecommerce.user.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.user.domain.event.AddressCreatedEvent;
import com.ecommerce.user.domain.event.AddressDeletedEvent;
import com.ecommerce.user.domain.event.AddressMarkedAsDefaultEvent;
import com.ecommerce.user.domain.event.AddressUpdatedEvent;
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
    private String recipientPhone;
    private String addressLine;
    private String ward;
    private String district;
    private String province;
    private boolean defaultAddress;

    public static Address create(UUID userId, String recipientName, String recipientPhone,
                                 String addressLine, String ward, String district,
                                 String province, boolean defaultAddress) {
        Instant now = Instant.now();
        return Address.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .userId(userId)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .addressLine(addressLine)
                .ward(ward)
                .district(district)
                .province(province)
                .defaultAddress(defaultAddress)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void update(String recipientName, String recipientPhone, String addressLine,
                       String ward, String district, String province) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.addressLine = addressLine;
        this.district = district;
        this.ward = ward;
        this.province = province;
        this.touchUpdate();
    }

    public void markAsDefault() {
        if (this.defaultAddress) return;
        this.defaultAddress = true;
        this.touchUpdate();
    }

    public void unmarkAsDefault() {
        if (!this.defaultAddress) return;
        this.defaultAddress = false;
        this.touchUpdate();
    }

    public String getFullAddress() {
        return String.format("%s, %s, %s, %s", addressLine, ward, district, province);
    }

    public AddressCreatedEvent toCreatedEvent() {
        return new AddressCreatedEvent(getId(), userId, getFullAddress(), defaultAddress, Instant.now());
    }

    public AddressUpdatedEvent toUpdatedEvent(UUID userId) {
        return new AddressUpdatedEvent(getId(), userId, Instant.now());
    }

    public AddressDeletedEvent toDeletedEvent(UUID userId) {
        return new AddressDeletedEvent(getId(), userId, Instant.now());
    }

    public AddressMarkedAsDefaultEvent toMarkedAsDefaultEvent(UUID userId) {
        return new AddressMarkedAsDefaultEvent(getId(), userId, Instant.now());
    }
}
