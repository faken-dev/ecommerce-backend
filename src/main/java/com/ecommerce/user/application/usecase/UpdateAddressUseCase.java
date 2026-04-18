package com.ecommerce.user.application.usecase;

import com.ecommerce.user.application.command.UpdateAddressCommand;
import com.ecommerce.user.application.dto.AddressResponse;
import com.ecommerce.user.domain.entity.Address;
import com.ecommerce.user.domain.repository.AddressRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateAddressUseCase {
    private final AddressRepository addressRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public AddressResponse execute(UUID userId, UUID addressId, UpdateAddressCommand command) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        address.update(
                command.recipientName(),
                command.recipientPhone(),
                command.addressLine(),
                command.ward(),
                command.district(),
                command.province()
        );

        Address saved = addressRepository.save(address);

        eventPublisher.publish(saved.toUpdatedEvent(userId));

        return AddressResponse.from(saved);
    }
}