package com.ecommerce.notification.application.port;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class NotificationMessage {
    private final String to;
    private final String subject;
    private final String templateName;
    private final Map<String, Object> variables;
    private final NotificationChannel channel;
}