package com.ecommerce.user.application.usecase;

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
public class SetDefaultAddressUseCase {

    private final AddressRepository addressRepository;
    private final UserProfileRepository userProfileRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public AddressResponse execute(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        // Unmark current default
        addressRepository.findDefaultByUserId(userId)
                .ifPresent(current -> {
                    current.unmarkAsDefault();
                    addressRepository.save(current);
                });

        // Mark new default
        address.markAsDefault();
        Address saved = addressRepository.save(address);

        // Update profile
        userProfileRepository.findByUserId(userId).ifPresent(profile -> {
            profile.updateProfile(
                    profile.getFullName(),
                    profile.getProfilePictureUrl(),
                    profile.getBio(),
                    profile.getDateOfBirth(),
                    profile.getGender(),
                    addressId
            );
            userProfileRepository.save(profile);
        });

        eventPublisher.publish(saved.toMarkedAsDefaultEvent(userId));

        return AddressResponse.from(saved);
    }
}
