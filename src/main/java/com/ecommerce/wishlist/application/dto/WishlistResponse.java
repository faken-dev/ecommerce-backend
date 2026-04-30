package com.ecommerce.wishlist.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    private UUID productId;
    private String name;
    private String slug;
    private BigDecimal price;
    private String imageUrl;
    private Instant addedAt;
}
