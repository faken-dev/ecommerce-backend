package com.ecommerce.shared.application.service;

import java.util.Collection;

public interface MediaAssetService {
    /**
     * Registers a new upload in the database with PENDING status.
     */
    void registerUpload(String url, String storageKey, Long fileSize, String mimeType);

    /**
     * Marks multiple assets as ACTIVE (linked to a resource).
     */
    void markAsActive(Collection<String> urls);

    /**
     * Deletes orphan PENDING assets from both DB and Cloud.
     */
    void cleanupOrphanAssets();
}
