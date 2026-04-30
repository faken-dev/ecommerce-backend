package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.command.UpdateCategoryCommand;
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
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CatalogApplicationMapper mapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public CategoryResponse execute(UpdateCategoryCommand command) {
        Category category = categoryRepository.findById(command.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsBySlugExcludingId(command.slug(), command.id())) {
            throw new BusinessException(ErrorCode.CATEGORY_SLUG_ALREADY_EXISTS);
        }

        category.updateSlug(command.slug());
        category.update(command.name(), command.description(), command.iconUrl(), command.sortOrder());
        Category saved = categoryRepository.save(category);
        eventPublisher.publish(saved.toUpdatedEvent());
        
        CategoryResponse response = mapper.toCategoryResponse(saved);
        if (saved.getParentId() != null) {
            String parentName = categoryRepository.findById(saved.getParentId())
                    .map(Category::getName)
                    .orElse(null);
            return enrichWithParentName(response, parentName);
        }
        return response;
    }

    private CategoryResponse enrichWithParentName(CategoryResponse base, String parentName) {
        return new CategoryResponse(
            base.id(), base.slug(), base.name(), base.description(),
            base.parentId(), parentName, base.iconUrl(), base.sortOrder(), base.active()
        );
    }
}
