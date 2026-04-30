package com.ecommerce.admin.application.dto;

import java.util.UUID;

public record BannerRequest(
        UUID id,
        String imageUrl,
        String linkUrl,
        String title,
        String status,
        Integer priority
) {}
