package com.ecommerce.user.application.dto;

import com.ecommerce.user.domain.entity.Address;

import java.time.Instant;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        UUID userId,
        String recipientName,
        String recipientPhone,
        String addressLine,
        String ward,
        String district,
        String province,
        String fullAddress,
        boolean defaultAddress,
        Instant createdAt,
        Instant updatedAt
) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getUserId(),
                address.getRecipientName(),
                address.getRecipientPhone(),
                address.getAddressLine(),
                address.getWard(),
                address.getDistrict(),
                address.getProvince(),
                address.getFullAddress(),
                address.isDefaultAddress(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}
