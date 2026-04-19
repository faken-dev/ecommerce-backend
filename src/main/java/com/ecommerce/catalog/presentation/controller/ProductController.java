package com.ecommerce.catalog.presentation.controller;

import com.ecommerce.catalog.application.command.ActivateProductCommand;
import com.ecommerce.catalog.application.command.AdjustStockCommand;
import com.ecommerce.catalog.application.command.CreateProductCommand;
import com.ecommerce.catalog.application.command.UpdateProductCommand;
import com.ecommerce.catalog.application.dto.ProductResponse;
import com.ecommerce.catalog.application.dto.ProductSummaryResponse;
import com.ecommerce.catalog.application.usecase.*;
import com.ecommerce.catalog.presentation.dto.request.CreateProductRequest;
import com.ecommerce.catalog.presentation.dto.request.UpdateProductRequest;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Product management.
 *
 * Public:  GET /public/*   (anyone can browse/search)
 * Seller:  POST / PUT / DELETE /stock  (owner only — SELLER or ADMIN role)
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product catalog management")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ActivateProductUseCase activateProductUseCase;
    private final AdjustStockUseCase adjustStockUseCase;
    private final SearchProductsUseCase searchProductsUseCase;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping("/public")
    @Operation(
        summary = "List active public products (paginated)",
        description = "Returns active products visible to shoppers, ordered by featured first."
    )
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> listActivePublic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ApiResponse<List<ProductSummaryResponse>> result =
                searchProductsUseCase.activePublic(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/public/search")
    @Operation(summary = "Search products by name or description")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchProductsUseCase.search(q, page, size));
    }

    @GetMapping("/public/category/{categoryId}")
    @Operation(summary = "List active products by category")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> byCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchProductsUseCase.byCategory(categoryId, page, size));
    }

    @GetMapping("/public/{productId}")
    @Operation(summary = "Get a single product by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<ProductResponse>> getPublicProduct(
            @PathVariable UUID productId) {
        ProductResponse response = getProductUseCase.execute(productId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ── Seller / Admin ────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('product:create')")
    @Operation(
        summary = "Create a new product",
        description = "Seller creates a product (starts as DRAFT). Slug must be unique per seller."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slug conflict")
    })
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal UUID sellerId,
            @Valid @RequestBody CreateProductRequest req) {
        // Force sellerId from JWT token — prevents spoofing sellerId in body
        ProductResponse response = createProductUseCase.execute(
                new CreateProductCommand(
                        sellerId,
                        req.name(),
                        req.slug(),
                        req.description(),
                        req.price(),
                        req.compareAtPrice(),
                        req.costPerItem(),
                        req.sku(),
                        req.barcode(),
                        req.categoryId(),
                        req.tags(),
                        req.visibility(),
                        req.metaTitle(),
                        req.metaDescription(),
                        req.weightKg(),
                        req.weightUnit()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Product created successfully"));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('product:read')")
    @Operation(summary = "List my products (seller dashboard)")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> myProducts(
            @AuthenticationPrincipal UUID sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchProductsUseCase.bySeller(sellerId, page, size));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('product:read')")
    @Operation(summary = "Get product details (seller view)")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable UUID productId) {
        ProductResponse response = getProductUseCase.execute(productId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('product:update')")
    @Operation(summary = "Update a product")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slug conflict")
    })
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest req) {
        ProductResponse response = updateProductUseCase.execute(
                new UpdateProductCommand(
                        productId,
                        req.name(),
                        req.slug(),
                        req.description(),
                        req.price(),
                        req.compareAtPrice(),
                        req.categoryId(),
                        req.tags(),
                        req.metaTitle(),
                        req.metaDescription()));
        return ResponseEntity.ok(ApiResponse.ok(response, "Product updated successfully"));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('product:delete')")
    @Operation(summary = "Soft-delete a product")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID productId) {
        deleteProductUseCase.execute(productId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Product deleted"));
    }

    @PostMapping("/{productId}/activate")
    @PreAuthorize("hasAuthority('product:activate')")
    @Operation(summary = "Activate a draft product (make it publicly visible)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Activated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<ProductResponse>> activateProduct(
            @PathVariable UUID productId) {
        ProductResponse response = activateProductUseCase.execute(
                new ActivateProductCommand(productId));
        return ResponseEntity.ok(ApiResponse.ok(response, "Product activated"));
    }

    @PatchMapping("/{productId}/stock")
    @PreAuthorize("hasAuthority('product:stock')")
    @Operation(
        summary = "Adjust stock quantity",
        description = "Adjusts stock by a delta (positive = increase, negative = decrease). " +
                      "Cannot reduce below 0."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient stock")
    })
    public ResponseEntity<ApiResponse<Void>> adjustStock(
            @PathVariable UUID productId,
            @RequestParam int delta) {
        adjustStockUseCase.execute(new AdjustStockCommand(productId, delta));
        return ResponseEntity.ok(ApiResponse.ok(null, "Stock adjusted"));
    }
}