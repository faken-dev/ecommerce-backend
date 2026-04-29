package com.ecommerce.auth.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OtpToken extends AuditableEntity {

    public enum Channel { EMAIL, SMS, WHATSAPP }
    public enum Purpose { EMAIL_VERIFICATION, PHONE_VERIFICATION, PASSWORD_RESET, LOGIN }

    private UUID userId;
    private String codeHash;
    private Channel channel;
    private Purpose purpose;
    private Instant expiresAt;
    private Instant usedAt;
    private int attemptCount;

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

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isUsed()   { return usedAt != null; }

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
