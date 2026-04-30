package com.ecommerce.shared.event.outbox;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shared_outbox_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant processedAt;

    private String error;

    @Version
    private Long version;

    public static OutboxEvent create(String aggregateType, String aggregateId, String eventType, String payload) {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public void markAsProcessed() {
        this.status = OutboxStatus.PROCESSED;
        this.processedAt = Instant.now();
    }

    public void markAsFailed(String error) {
        this.status = OutboxStatus.FAILED;
        this.error = error;
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public OutboxStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public String getError() { return error; }
    public Long getVersion() { return version; }

    public void setId(UUID id) { this.id = id; }

    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public void setEventType(String eventType) { this.eventType = eventType; }

    public void setPayload(String payload) { this.payload = payload; }

    public void setStatus(OutboxStatus status) { this.status = status; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public void setError(String error) { this.error = error; }

    public void setVersion(Long version) { this.version = version; }
}
