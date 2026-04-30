package com.ecommerce.shared.infrastructure.service;

import com.ecommerce.shared.application.service.MediaAssetService;
import com.ecommerce.shared.application.service.StorageService;
import com.ecommerce.shared.infrastructure.persistence.entity.MediaAssetJpaEntity;
import com.ecommerce.shared.infrastructure.persistence.repository.MediaAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaAssetServiceImpl implements MediaAssetService {

    private final MediaAssetRepository mediaAssetRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public void registerUpload(String url, String storageKey, Long fileSize, String mimeType) {
        MediaAssetJpaEntity asset = MediaAssetJpaEntity.builder()
                .url(url)
                .storageKey(storageKey)
                .fileSize(fileSize)
                .mimeType(mimeType)
                .status("PENDING")
                .build();
        mediaAssetRepository.save(asset);
        log.debug("Registered pending upload: {}", url);
    }

    @Override
    @Transactional
    public void markAsActive(Collection<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        mediaAssetRepository.markAsActive(urls);
        log.debug("Marked {} assets as ACTIVE", urls.size());
    }

    @Override
    @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM every day
    @Transactional
    public void cleanupOrphanAssets() {
        log.info("Starting orphan media assets cleanup...");
        
        // Find PENDING assets older than 24 hours
        Instant threshold = Instant.now().minus(24, ChronoUnit.HOURS);
        List<MediaAssetJpaEntity> orphans = mediaAssetRepository.findByStatusAndCreatedAtBefore("PENDING", threshold);
        
        if (orphans.isEmpty()) {
            log.info("No orphan assets found for cleanup.");
            return;
        }

        log.info("Found {} orphan assets to delete.", orphans.size());
        
        for (MediaAssetJpaEntity asset : orphans) {
            try {
                // 1. Delete from Cloud
                storageService.deleteFile(asset.getUrl());
                
                // 2. Delete from DB
                mediaAssetRepository.delete(asset);
                
                log.debug("Cleaned up orphan asset: {}", asset.getUrl());
            } catch (Exception e) {
                log.error("Failed to cleanup orphan asset: {}", asset.getUrl(), e);
            }
        }
        
        log.info("Orphan assets cleanup completed.");
    }
}
