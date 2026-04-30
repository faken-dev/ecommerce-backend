package com.ecommerce.shared.event.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class OutboxService {
    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Saves an event to the outbox table.
     * MUST be called within an active transaction to ensure atomicity with business logic.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveEvent(Object event, String aggregateId, String aggregateType) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String eventType = event.getClass().getName();

            OutboxEvent outboxEvent = OutboxEvent.create(
                    aggregateType,
                    aggregateId,
                    eventType,
                    payload
            );

            outboxRepository.save(outboxEvent);
            log.debug("Event {} saved to outbox for aggregate {}/{}", eventType, aggregateType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {} for outbox", event.getClass().getSimpleName(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }
}
