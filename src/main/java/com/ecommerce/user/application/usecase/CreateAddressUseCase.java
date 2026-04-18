package com.ecommerce.user.application.usecase;

import com.ecommerce.user.application.command.CreateAddressCommand;
import com.ecommerce.user.application.dto.AddressResponse;
import com.ecommerce.user.domain.entity.Address;
import com.ecommerce.user.domain.repository.AddressRepository;
import com.ecommerce.user.domain.repository.UserProfileRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateAddressUseCase {

    private final AddressRepository addressRepository;
    private final UserProfileRepository userProfileRepository;
    private final EventPublisher eventPublisher;

    private static final int MAX_ADDRESSES_PER_USER = 10;

    @Transactional
    public AddressResponse execute(UUID userId, CreateAddressCommand command) {
        long currentCount = addressRepository.countByUserId(userId);
        if (currentCount >= MAX_ADDRESSES_PER_USER) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_LIMIT_EXCEEDED);
        }

        boolean isFirst = currentCount == 0;
        boolean shouldBeDefault = isFirst || command.isDefault();

        // Unmark current default if setting new default
        if (shouldBeDefault && !isFirst) {
            addressRepository.findDefaultByUserId(userId)
                    .ifPresent(current -> {
                        current.unmarkAsDefault();
                        addressRepository.save(current);
                    });
        }

        Address address = Address.create(
                userId,
                command.recipientName(),
                command.recipientPhone(),
                command.addressLine(),
                command.ward(),
                command.district(),
                command.province(),
                shouldBeDefault
        );

        Address saved = addressRepository.save(address);

        // Update profile default address if first
        if (isFirst) {
            userProfileRepository.findByUserId(userId).ifPresent(profile -> {
                profile.updateProfile(
                        profile.getFullName(),
                        profile.getProfilePictureUrl(),
                        profile.getBio(),
                        profile.getDateOfBirth(),
                        profile.getGender(),
                        saved.getId()
                );
                userProfileRepository.save(profile);
            });

            eventPublisher.publish(saved.toCreatedEvent());
        }

        return AddressResponse.from(saved);
    }
}