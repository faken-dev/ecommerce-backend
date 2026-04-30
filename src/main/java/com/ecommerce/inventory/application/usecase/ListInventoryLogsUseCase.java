package com.ecommerce.inventory.application.usecase;

import com.ecommerce.inventory.infrastructure.persistence.repository.InventoryLogJpaRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListInventoryLogsUseCase {

    private final InventoryLogJpaRepository logRepository;

    @Getter
    public static class InventoryLogResponse {
        private UUID id;
        private UUID productId;
        private int changeAmount;
        private int stockAfter;
        private String actionType;
        private String reason;
        private Instant createdAt;

        public UUID getId() { return id; }
        public UUID getProductId() { return productId; }
        public int getChangeAmount() { return changeAmount; }
        public int getStockAfter() { return stockAfter; }
        public String getActionType() { return actionType; }
        public String getReason() { return reason; }
        public Instant getCreatedAt() { return createdAt; }

        public static InventoryLogResponseBuilder builder() { return new InventoryLogResponseBuilder(); }
        public static class InventoryLogResponseBuilder {
            private UUID id;
            private UUID productId;
            private int changeAmount;
            private int stockAfter;
            private String actionType;
            private String reason;
            private Instant createdAt;
            public InventoryLogResponseBuilder id(UUID id) { this.id = id; return this; }
            public InventoryLogResponseBuilder productId(UUID productId) { this.productId = productId; return this; }
            public InventoryLogResponseBuilder changeAmount(int changeAmount) { this.changeAmount = changeAmount; return this; }
            public InventoryLogResponseBuilder stockAfter(int stockAfter) { this.stockAfter = stockAfter; return this; }
            public InventoryLogResponseBuilder actionType(String actionType) { this.actionType = actionType; return this; }
            public InventoryLogResponseBuilder reason(String reason) { this.reason = reason; return this; }
            public InventoryLogResponseBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
            public InventoryLogResponse build() {
                InventoryLogResponse r = new InventoryLogResponse();
                r.id = id; r.productId = productId; r.changeAmount = changeAmount; r.stockAfter = stockAfter;
                r.actionType = actionType; r.reason = reason; r.createdAt = createdAt;
                return r;
            }
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryLogResponse> execute(UUID productId) {
        return logRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(log -> InventoryLogResponse.builder()
                        .id(log.getId())
                        .productId(log.getProductId())
                        .changeAmount(log.getChangeAmount())
                        .stockAfter(log.getStockAfter())
                        .actionType(log.getActionType())
                        .reason(log.getReason())
                        .createdAt(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
