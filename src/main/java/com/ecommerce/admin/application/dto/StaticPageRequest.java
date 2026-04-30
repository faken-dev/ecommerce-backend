package com.ecommerce.admin.application.dto;

import java.util.UUID;

public record StaticPageRequest(
    UUID id,
    String title,
    String slug,
    String content,
    Boolean isActive
) {}
