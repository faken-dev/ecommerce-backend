package com.ecommerce.shared.presentation.controller;

import com.ecommerce.shared.application.service.MediaAssetService;
import com.ecommerce.shared.application.service.StorageService;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Tag(name = "Storage", description = "File storage and management")
public class StorageController {

    private final StorageService storageService;
    private final MediaAssetService mediaAssetService;

    @Operation(summary = "Upload a file")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()") // Only logged in users can upload
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) throws IOException {
        
        String url = storageService.uploadFile(file, folder);
        
        // Register as PENDING
        mediaAssetService.registerUpload(
            url, 
            storageService.getKeyFromUrl(url), 
            file.getSize(), 
            file.getContentType()
        );
        
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url), "Upload successful"));
    }

    @Operation(summary = "Delete a file")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('product:update') or hasAuthority('cms:manage')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam("url") String url) {
        storageService.deleteFile(url);
        return ResponseEntity.ok(ApiResponse.success("Delete successful"));
    }
}


