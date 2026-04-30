package com.ecommerce.shared.event.outbox;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class OutboxProcessor {
    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.polling-interval-ms:1000}")
    public void processPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents(PageRequest.of(0, 50));
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", pendingEvents.size());
        for (OutboxEvent event : pendingEvents) {
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent outboxEvent) {
        try {
            Class<?> eventClass = Class.forName(outboxEvent.getEventType());
            Object event = objectMapper.readValue(outboxEvent.getPayload(), eventClass);

            applicationEventPublisher.publishEvent(event);
            
            outboxEvent.markAsProcessed();
            outboxRepository.save(outboxEvent);
            log.debug("Outbox event {} processed successfully", outboxEvent.getId());
        } catch (Exception e) {
            log.error("Failed to process outbox event {}", outboxEvent.getId(), e);
            outboxEvent.markAsFailed(e.getMessage());
            outboxRepository.save(outboxEvent);
        }
    }
}
