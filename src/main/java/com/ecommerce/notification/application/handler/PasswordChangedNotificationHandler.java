package com.ecommerce.notification.application.handler;

import com.ecommerce.auth.domain.event.PasswordChangedEvent;
import com.ecommerce.notification.application.port.NotificationChannel;
import com.ecommerce.notification.application.port.NotificationMessage;
import com.ecommerce.notification.application.port.NotificationSender;
import com.ecommerce.notification.domain.NotificationTemplate;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Sends a security notification email when the user changes their password.
 * This allows users to detect unauthorized password changes immediately.
 */
@Slf4j
@Component
public class PasswordChangedNotificationHandler {

    private final NotificationSender emailSender;

        public PasswordChangedNotificationHandler(
            @Qualifier("sendGridEmailSender") NotificationSender emailSender
    ) {
        this.emailSender = emailSender;
    }


    /**
     * Fires AFTER the transaction commits — ensures the password was actually changed
     * before sending the notification email. Prevents false-positive emails when
     * the transaction rolls back after the event is published.
     */
    @Async
    @org.springframework.transaction.event.TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PasswordChangedEvent event) {
        String subject = NotificationTemplate.PASSWORD_CHANGED_EMAIL.getSubject();
        NotificationMessage message = NotificationMessage.builder()
                .to(event.email())
                .subject(subject)
                .templateName(NotificationTemplate.PASSWORD_CHANGED_EMAIL.getTemplateName())
                .variables(java.util.Map.of(
                        "fullName", event.fullName(),
                        "changedAt", DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
                                .format(event.occurredAt().atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDateTime())
                ))
                .channel(NotificationChannel.EMAIL)
                .build();

        try {
            emailSender.send(message);
            log.info("Password changed notification sent to {}", maskEmail(event.email()));
        } catch (Exception e) {
            log.error("Failed to send password changed notification to {}: {}",
                    event.email(), e.getMessage());
            // Security notification failure is non-critical — don't block the password change
        }
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at < 0) return "****";
        return email.substring(0, 2) + "**" + email.substring(at);
    }
}