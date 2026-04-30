package com.ecommerce.voucher.infrastructure.persistence.entity;

import com.ecommerce.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "voucher_user_vouchers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "voucher_id"})
})
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserVoucherJpaEntity extends AuditableJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private VoucherJpaEntity voucher;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public VoucherJpaEntity getVoucher() { return voucher; }
    public void setVoucher(VoucherJpaEntity voucher) { this.voucher = voucher; }
    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public boolean getUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public boolean isUsed() { return used; }
}
