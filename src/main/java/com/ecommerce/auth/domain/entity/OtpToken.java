package com.ecommerce.auth.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Getter;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Getter
public class OtpToken extends AuditableEntity {

    public enum Channel { EMAIL, SMS, WHATSAPP }
    public enum Purpose { EMAIL_VERIFICATION, PHONE_VERIFICATION, PASSWORD_RESET, LOGIN }

    private final UUID userId;
    /** HMAC-SHA256 hash of the raw OTP. 64 hex characters. */
    private final String codeHash;
    private final Channel channel;
    private final Purpose purpose;
    private final Instant expiresAt;
    private Instant usedAt;
    /** Incremented on each failed verification attempt. */
    private int attemptCount;

    /** Creates a new OTP token. */
    public static OtpToken create(UUID userId, String codeHash, Channel channel,
                                  Purpose purpose, int expiryMinutes) {
        return OtpToken.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .userId(userId)
                .codeHash(codeHash)
                .channel(channel)
                .purpose(purpose)
                .expiresAt(Instant.now().plusSeconds(expiryMinutes * 60L))
                .attemptCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Builder
    public OtpToken(UUID id, UUID userId, String codeHash, Channel channel,
                    Purpose purpose, Instant expiresAt, Instant usedAt, int attemptCount,
                    Instant createdAt, Instant updatedAt, UUID createdBy, UUID updatedBy) {
        super(id, createdAt, updatedAt, createdBy, updatedBy);
        this.userId = userId;
        this.codeHash = codeHash;
        this.channel = channel;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.attemptCount = attemptCount;
    }

    // ── Domain Rules ──────────────────────────────────────────────────────────

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isUsed()   { return usedAt != null; }

    /** Throws BusinessException if expired, already used, or max attempts exceeded. */
    public void validateForVerification(int maxAttempts) {
        if (isExpired()) throw new BusinessException(ErrorCode.OTP_EXPIRED);
        if (isUsed())    throw new BusinessException(ErrorCode.OTP_ALREADY_USED);
        this.attemptCount++;
        if (this.attemptCount > maxAttempts) {
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }
    }

    public void markAsUsed()            { this.usedAt = Instant.now(); }
    public void incrementFailedAttempt() { this.attemptCount++; }
}