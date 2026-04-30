package com.ecommerce.notification.application.handler;

import com.ecommerce.auth.domain.event.OtpRequestedEvent;
import com.ecommerce.auth.domain.event.WelcomeEmailRequestedEvent;
import com.ecommerce.notification.application.port.NotificationChannel;
import com.ecommerce.notification.application.port.NotificationMessage;
import com.ecommerce.notification.application.port.NotificationSender;
import com.ecommerce.notification.domain.NotificationTemplate;
import com.ecommerce.notification.infrastructure.NotificationDlqRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class OtpNotificationHandler {
    private static final Logger log = LoggerFactory.getLogger(OtpNotificationHandler.class);

    private final List<NotificationSender> senders;
    private final NotificationDlqRepository dlqRepository;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    /**
     * Handles OtpRequestedEvent - sends OTP via EMAIL / SMS / WhatsApp.
     */
    @Async
    @EventListener
    public void handle(OtpRequestedEvent event) {
        String destination = event.destination();
        if (destination == null || destination.isBlank()) {
            log.error("No destination in OtpRequestedEvent for userId={}", event.userId());
            return;
        }

        String rawOtp = event.otpCode();
        if (rawOtp == null || rawOtp.isBlank()) {
            log.error("No OTP code in OtpRequestedEvent for userId={}", event.userId());
            dlqRepository.push(
                    buildFallbackMessage(event, destination),
                    "MISSING_OTP_CODE_IN_EVENT");
            return;
        }

        NotificationChannel channel = NotificationChannel.from(event.channel());
        NotificationMessage message = buildOtpMessage(rawOtp, event, channel, destination);

        senders.stream()
                .filter(s -> s.supports(channel))
                .findFirst()
                .ifPresentOrElse(
                        sender -> {
                            try {
                                sender.send(message);
                                log.info("OTP sent via {} to {} for userId={}, purpose={}",
                                        channel, maskDestination(destination),
                                        event.userId(), event.purpose());
                            } catch (Exception e) {
                                // Sender throw sau khi retry exhaust → ghi DLQ
                                log.error("OTP send failed via {}, queuing to DLQ: {}",
                                        channel, e.getMessage());
                                dlqRepository.push(message, "OTP_SEND_FAILED: " + e.getMessage());
                            }
                        },
                        () -> {
                            log.error("No sender found for channel: {}", channel);
                            dlqRepository.push(
                                    buildFallbackMessage(event, destination),
                                    "NO_SENDER: " + channel);
                        }
                );
    }

    /**
     * Handles WelcomeEmailRequestedEvent - sends welcome email after verify.
     */
    @Async
    @EventListener
    public void handle(WelcomeEmailRequestedEvent event) {
        NotificationMessage message = NotificationMessage.builder()
                .to(event.email())
                .subject(NotificationTemplate.WELCOME_EMAIL.getSubject())
                .templateName(NotificationTemplate.WELCOME_EMAIL.getTemplateName())
                .variables(Map.of("fullName", event.fullName()))
                .channel(NotificationChannel.EMAIL)
                .build();

        senders.stream()
                .filter(s -> s.supports(NotificationChannel.EMAIL))
                .findFirst()
                .ifPresent(sender -> {
                    try {
                        sender.send(message);
                        log.info("Welcome email sent to {}", maskEmail(event.email()));
                    } catch (Exception e) {
                        log.error("Welcome email failed, queuing to DLQ: {}",
                                e.getMessage());
                        dlqRepository.push(message, "WELCOME_EMAIL_FAILED: " + e.getMessage());
                    }
                });
    }

    // Helpers

    private NotificationMessage buildOtpMessage(String rawOtp,
                                                OtpRequestedEvent event,
                                                NotificationChannel channel,
                                                String destination) {
        return switch (channel) {
            case EMAIL -> NotificationMessage.builder()
                    .to(destination)
                    .subject(NotificationTemplate.OTP_EMAIL.getSubject())
                    .templateName(NotificationTemplate.OTP_EMAIL.getTemplateName())
                    .variables(Map.of(
                            "code", rawOtp,
                            "purpose", event.purpose().name(),
                            "minutes", otpExpiryMinutes
                    ))
                    .channel(channel)
                    .build();

            case SMS, WHATSAPP -> NotificationMessage.builder()
                    .to(destination)
                    .templateName(NotificationTemplate.OTP_SMS.getTemplateName()
                            .replace("{code}", rawOtp)
                            .replace("{minutes}", String.valueOf(otpExpiryMinutes)))
                    .variables(Map.of())
                    .channel(channel)
                    .build();
        };
    }

    private NotificationMessage buildFallbackMessage(OtpRequestedEvent event, String destination) {
        return NotificationMessage.builder()
                .to(destination)
                .subject("[Action Required] Unable to send OTP")
                .templateName(null)
                .variables(Map.of("userId", event.userId().toString()))
                .channel(NotificationChannel.from(event.channel()))
                .build();
    }

    private String maskDestination(String destination) {
        if (destination == null) return "****";
        if (destination.contains("@")) {
            int atIndex = destination.indexOf('@');
            return destination.substring(0, 2) + "**" + destination.substring(atIndex);
        }
        if (destination.startsWith("+")) {
            return destination.substring(0, 4) + "****"
                    + destination.substring(destination.length() - 3);
        }
        return "****";
    }

    private String maskEmail(String email) {
        return maskDestination(email);
    }
}
