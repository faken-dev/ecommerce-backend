package com.ecommerce.catalog.presentation.controller;

import com.ecommerce.catalog.application.command.CreateCategoryCommand;
import com.ecommerce.catalog.application.command.UpdateCategoryCommand;
import com.ecommerce.catalog.application.dto.CategoryResponse;
import com.ecommerce.catalog.application.dto.CategoryTreeResponse;
import com.ecommerce.catalog.application.usecase.CreateCategoryUseCase;
import com.ecommerce.catalog.application.usecase.DeleteCategoryUseCase;
import com.ecommerce.catalog.application.usecase.GetCategoryTreeUseCase;
import com.ecommerce.catalog.application.usecase.UpdateCategoryUseCase;
import com.ecommerce.catalog.presentation.dto.request.CreateCategoryRequest;
import com.ecommerce.catalog.presentation.dto.request.UpdateCategoryRequest;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Category management — public browsing + admin CRUD.
 *
 * Public:  GET /tree
 * Admin:   POST / PUT / DELETE  (ADMIN or SELLER role)
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final GetCategoryTreeUseCase getCategoryTreeUseCase;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping("/tree")
    @Operation(
        summary = "Get category tree",
        description = "Returns all active categories as a nested tree for frontend navigation menus."
    )
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        List<CategoryTreeResponse> tree = getCategoryTreeUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(tree, "Category tree fetched"));
    }

    // ── Admin / Seller ────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('category:create')")
    @Operation(summary = "Create a new category")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slug already exists")
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest req) {
        CategoryResponse response = createCategoryUseCase.execute(
                new CreateCategoryCommand(
                        req.slug(),
                        req.name(),
                        req.description(),
                        req.parentId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Category created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category:update')")
    @Operation(summary = "Update a category")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slug conflict")
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Category ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest req) {
        CategoryResponse response = updateCategoryUseCase.execute(
                new UpdateCategoryCommand(
                        id,
                        req.slug(),
                        req.name(),
                        req.description(),
                        req.iconUrl(),
                        req.sortOrder()));
        return ResponseEntity.ok(ApiResponse.ok(response, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:delete')")
    @Operation(
        summary = "Deactivate a category",
        description = "Soft-deactivates the category. Fails if it has active children."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deactivated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Has children")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable UUID id) {
        deleteCategoryUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Category deactivated"));
    }
}