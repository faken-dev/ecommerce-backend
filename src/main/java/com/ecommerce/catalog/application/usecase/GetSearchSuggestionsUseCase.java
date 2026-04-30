package com.ecommerce.catalog.application.usecase;


import com.ecommerce.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetSearchSuggestionsUseCase {
    private final ProductJpaRepository productJpaRepository;

    @Transactional(readOnly = true)
    public List<String> execute(String query) {
        if (query == null || query.length() < 2) {
            return List.of();
        }
        
        // Find top 10 product names matching query
        return productJpaRepository.search(query, null, PageRequest.of(0, 10))
                .getContent()
                .stream()
                .map(p -> p.getName())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }
}
