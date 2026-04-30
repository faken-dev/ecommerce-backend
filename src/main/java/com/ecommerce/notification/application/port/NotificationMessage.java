package com.ecommerce.notification.application.port;

import java.util.Map;

public class NotificationMessage {
    private final String to;
    private final String subject;
    private final String templateName;
    private final Map<String, Object> variables;
    private final NotificationChannel channel;

    public NotificationMessage(String to, String subject, String templateName, Map<String, Object> variables, NotificationChannel channel) {
        this.to = to;
        this.subject = subject;
        this.templateName = templateName;
        this.variables = variables;
        this.channel = channel;
    }

    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public String getTemplateName() { return templateName; }
    public Map<String, Object> getVariables() { return variables; }
    public NotificationChannel getChannel() { return channel; }

    public static NotificationMessageBuilder builder() {
        return new NotificationMessageBuilder();
    }

    public static class NotificationMessageBuilder {
        private String to;
        private String subject;
        private String templateName;
        private Map<String, Object> variables;
        private NotificationChannel channel;

        public NotificationMessageBuilder to(String to) { this.to = to; return this; }
        public NotificationMessageBuilder subject(String subject) { this.subject = subject; return this; }
        public NotificationMessageBuilder templateName(String templateName) { this.templateName = templateName; return this; }
        public NotificationMessageBuilder variables(Map<String, Object> variables) { this.variables = variables; return this; }
        public NotificationMessageBuilder channel(NotificationChannel channel) { this.channel = channel; return this; }

        public NotificationMessage build() {
            return new NotificationMessage(to, subject, templateName, variables, channel);
        }
    }
}
