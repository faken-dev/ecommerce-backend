package com.ecommerce.order.infrastructure.adapter;

import com.ecommerce.order.application.port.ShippingQueryPort;
import com.ecommerce.user.infrastructure.persistence.entity.AddressJpaEntity;
import com.ecommerce.user.infrastructure.persistence.repository.AddressJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShippingQueryAdapter implements ShippingQueryPort {

    private final AddressJpaRepository addressRepository;

    @Override
    public String getFullAddress(UUID addressId) {
        return addressRepository.findById(addressId)
                .map(this::formatAddress)
                .orElse("N/A");
    }

    private String formatAddress(AddressJpaEntity entity) {
        return String.format("%s, %s, %s, %s, %s (%s)",
                entity.getRecipientName(),
                entity.getAddressLine(),
                entity.getWard(),
                entity.getDistrict(),
                entity.getProvince(),
                entity.getRecipientPhone());
    }
}
