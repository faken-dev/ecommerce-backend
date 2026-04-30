package com.ecommerce.voucher.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor
public class UserVoucher extends AuditableEntity {

    private UUID userId;
    private Voucher voucher;
    private Instant collectedAt;
    private Instant usedAt;
    private boolean used;

    public void markAsUsed() {
        this.used = true;
        this.usedAt = Instant.now();
    }

    public UUID getUserId() { return userId; }
    public Voucher getVoucher() { return voucher; }
    public Instant getCollectedAt() { return collectedAt; }
    public Instant getUsedAt() { return usedAt; }
    public boolean getUsed() { return used; }

    public boolean isUsed() { return used; }
}
