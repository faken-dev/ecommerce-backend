package com.ecommerce.catalog.application.usecase;

import com.ecommerce.catalog.application.dto.CategoryTreeResponse;
import com.ecommerce.catalog.application.mapper.CatalogApplicationMapper;
import com.ecommerce.catalog.domain.entity.Category;
import com.ecommerce.catalog.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GetCategoryTreeUseCase {

    private final CategoryRepository categoryRepository;
    private final CatalogApplicationMapper mapper;

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> execute() {
        List<Category> all = categoryRepository.findAllActive();
        Map<UUID, List<Category>> childrenMap = new HashMap<>();
        List<Category> roots = new ArrayList<>();

        for (Category c : all) {
            if (c.getParentId() == null) {
                roots.add(c);
            } else {
                childrenMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
            }
        }

        return roots.stream()
                .map(root -> buildTree(root, childrenMap))
                .sorted(Comparator.comparingInt(CategoryTreeResponse::sortOrder))
                .toList();
    }

    private CategoryTreeResponse buildTree(Category category, Map<UUID, List<Category>> childrenMap) {
        List<CategoryTreeResponse> children = childrenMap
                .getOrDefault(category.getId(), List.of()).stream()
                .sorted(Comparator.comparingInt(Category::getSortOrder))
                .map(child -> buildTree(child, childrenMap))
                .toList();
        return mapper.toCategoryTreeResponse(category, children);
    }
}