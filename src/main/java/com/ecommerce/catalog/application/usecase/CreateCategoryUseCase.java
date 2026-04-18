package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.command.CreateCategoryCommand;
import com.ecommerce.catalog.application.dto.CategoryResponse;
import com.ecommerce.catalog.application.mapper.CatalogApplicationMapper;
import com.ecommerce.catalog.domain.entity.Category;
import com.ecommerce.catalog.domain.repository.CategoryRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CatalogApplicationMapper mapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public CategoryResponse execute(CreateCategoryCommand command) {
        if (categoryRepository.existsBySlug(command.slug())) {
            throw new BusinessException(ErrorCode.CATEGORY_SLUG_ALREADY_EXISTS);
        }

        Category category = command.parentId() != null
                ? Category.createChild(command.slug(), command.name(),
                  command.description(), command.parentId()).build()
                : Category.createRoot(command.slug(), command.name(),
                  command.description()).build();

        Category saved = categoryRepository.save(category);
        eventPublisher.publish(saved.toCreatedEvent());
        return mapper.toCategoryResponse(saved);
    }
}