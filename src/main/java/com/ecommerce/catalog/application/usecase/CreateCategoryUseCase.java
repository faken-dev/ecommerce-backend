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
                  command.description(), command.parentId())
                : Category.createRoot(command.slug(), command.name(),
                  command.description());

        Category saved = categoryRepository.save(category);
        eventPublisher.publish(saved.toCreatedEvent());
        
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
