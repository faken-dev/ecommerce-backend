package com.ecommerce.user.application.usecase;

import com.ecommerce.user.application.dto.AddressResponse;
import com.ecommerce.user.domain.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAddressesUseCase {

    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> execute(UUID userId) {
        return addressRepository.findByUserIdOrderByDefault(userId)
                .stream()
                .map(AddressResponse::from)
                .toList();
    }
}