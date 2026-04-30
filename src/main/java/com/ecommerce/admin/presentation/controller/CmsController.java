package com.ecommerce.admin.presentation.controller;

import com.ecommerce.admin.application.dto.BannerRequest;
import com.ecommerce.admin.application.dto.BannerResponse;
import com.ecommerce.admin.application.dto.StaticPageRequest;
import com.ecommerce.admin.application.dto.StaticPageResponse;
import com.ecommerce.admin.application.usecase.*;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "CMS", description = "Content Management System for banners and public content")
public class CmsController {

    private final GetActiveBannersUseCase getActiveBannersUseCase;
    private final GetAllBannersUseCase getAllBannersUseCase;
    private final SaveBannerUseCase saveBannerUseCase;
    private final DeleteBannerUseCase deleteBannerUseCase;
    private final GetAllStaticPagesUseCase getAllStaticPagesUseCase;
    private final GetStaticPageBySlugUseCase getStaticPageBySlugUseCase;
    private final SaveStaticPageUseCase saveStaticPageUseCase;

    @GetMapping("/public/banners")
    @Operation(summary = "Get all active banners for the home page")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getActiveBanners() {
        return ResponseEntity.ok(ApiResponse.ok(getActiveBannersUseCase.execute()));
    }

    @GetMapping("/admin/cms/banners")
    @PreAuthorize("hasAuthority('cms:manage')")
    @Operation(summary = "Admin: Get all banners")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getAllBanners() {
        return ResponseEntity.ok(ApiResponse.ok(getAllBannersUseCase.execute()));
    }

    @PostMapping("/admin/cms/banners")
    @PreAuthorize("hasAuthority('cms:manage')")
    @Operation(summary = "Admin: Create or update a banner")
    public ResponseEntity<ApiResponse<BannerResponse>> saveBanner(@RequestBody BannerRequest banner) {
        return ResponseEntity.ok(ApiResponse.ok(saveBannerUseCase.execute(banner)));
    }

    @DeleteMapping("/admin/cms/banners/{id}")
    @PreAuthorize("hasAuthority('cms:manage')")
    @Operation(summary = "Admin: Delete a banner")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable UUID id) {
        deleteBannerUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success("Banner deleted successfully"));
    }

    // Static Pages
    @GetMapping("/public/pages/{slug}")
    @Operation(summary = "Get static page by slug")
    public ResponseEntity<ApiResponse<StaticPageResponse>> getPageBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(getStaticPageBySlugUseCase.execute(slug)));
    }

    @GetMapping("/admin/cms/pages")
    @PreAuthorize("hasAuthority('cms:manage')")
    @Operation(summary = "Admin: Get all static pages")
    public ResponseEntity<ApiResponse<List<StaticPageResponse>>> getAllPages() {
        return ResponseEntity.ok(ApiResponse.ok(getAllStaticPagesUseCase.execute()));
    }

    @PostMapping("/admin/cms/pages")
    @PreAuthorize("hasAuthority('cms:manage')")
    @Operation(summary = "Admin: Create or update a static page")
    public ResponseEntity<ApiResponse<StaticPageResponse>> savePage(@RequestBody StaticPageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(saveStaticPageUseCase.execute(request)));
    }
}


