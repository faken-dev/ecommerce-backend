package com.ecommerce.notification.infrastructure;

import com.ecommerce.notification.application.port.NotificationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class NotificationDlqRepository {
    private static final Logger log = LoggerFactory.getLogger(NotificationDlqRepository.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DLQ_KEY_PREFIX = "notification:dlq:";
    private static final String DLQ_SET_KEY     = "notification:dlq:ids";  // Sorted set of IDs by timestamp
    private static final Duration DLQ_TTL        = Duration.ofDays(7);

    /**
     * Push failed notification into DLQ.
     */
    public void push(NotificationMessage message, String reason) {
        DlqEntry entry = new DlqEntry(
                UUID.randomUUID().toString(),
                message.getChannel().name(),
                message.getTo(),
                message.getSubject(),
                message.getTemplateName(),
                objectMapper.valueToTree(message.getVariables()),
                reason,
                Instant.now().toString(),
                0
        );

        try {
            String json = objectMapper.writeValueAsString(entry);
            String key = DLQ_KEY_PREFIX + entry.id();

            redisTemplate.opsForValue().set(key, json, DLQ_TTL);
            redisTemplate.opsForZSet().add(DLQ_SET_KEY, entry.id(),
                    Instant.now().toEpochMilli());

            log.info("Notification queued to DLQ: id={}, reason={}", entry.id(), reason);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize DLQ entry: {}", e.getMessage());
        }
    }

    /**
     * Read all entries from DLQ for admin review.
     */
    public List<DlqEntry> findAll() {
        var ids = redisTemplate.opsForZSet().range(DLQ_SET_KEY, 0, -1);
        if (ids == null || ids.isEmpty()) return List.of();

        return ids.stream()
                .map(id -> {
                    String json = redisTemplate.opsForValue().get(DLQ_KEY_PREFIX + id);
                    if (json == null) return null;
                    try {
                        return objectMapper.readValue(json, DlqEntry.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Delete an entry from DLQ after it has been reviewed and handled (e.g. retried or marked as won't fix).
     */
    public void remove(String id) {
        redisTemplate.delete(DLQ_KEY_PREFIX + id);
        redisTemplate.opsForZSet().remove(DLQ_SET_KEY, id);
    }

    // DLQ Entry Record

    public record DlqEntry(
            String id,
            String channel,
            String to,
            String subject,
            String templateName,
            JsonNode variables,
            String reason,
            String failedAt,
            int retryCount
    ) {}
}
