package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.domain.entity.Category;
import com.ecommerce.catalog.domain.repository.CategoryRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void execute(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // Check if category has active children
        if (!categoryRepository.findByParentId(categoryId).isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }

        category.deactivate();
        categoryRepository.save(category);
        eventPublisher.publish(category.toDeletedEvent());
    }
}