package com.ecommerce.user.application.usecase;

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
public class DeleteAddressUseCase {

    private final AddressRepository addressRepository;
    private final UserProfileRepository userProfileRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void execute(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        UUID defaultAddressId = address.getId();
        addressRepository.delete(address);

        eventPublisher.publish(address.toDeletedEvent(userId));

        // Update profile if deleted address was default
        userProfileRepository.findByUserId(userId).ifPresent(profile -> {
            if (defaultAddressId.equals(profile.getDefaultAddressId())) {
                addressRepository.findByUserIdOrderByDefault(userId)
                        .stream()
                        .findFirst()
                        .ifPresentOrElse(
                                newDefault -> {
                                    profile.updateProfile(
                                            profile.getFullName(),
                                            profile.getProfilePictureUrl(),
                                            profile.getBio(),
                                            profile.getDateOfBirth(),
                                            profile.getGender(),
                                            newDefault.getId()
                                    );
                                    userProfileRepository.save(profile);
                                },
                                () -> {
                                    profile.updateProfile(
                                            profile.getFullName(),
                                            profile.getProfilePictureUrl(),
                                            profile.getBio(),
                                            profile.getDateOfBirth(),
                                            profile.getGender(),
                                            null
                                    );
                                    userProfileRepository.save(profile);
                                }
                        );
            }
        });
    }
}