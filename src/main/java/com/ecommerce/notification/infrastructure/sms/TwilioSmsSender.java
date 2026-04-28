package com.ecommerce.notification.infrastructure.sms;

import com.ecommerce.notification.application.port.NotificationChannel;
import com.ecommerce.notification.application.port.NotificationMessage;
import com.ecommerce.notification.application.port.NotificationSender;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TwilioSmsSender implements NotificationSender {

    @Value("${notification.twilio.from-phone}")
    private String fromPhone;

    @Value("${notification.twilio.whatsapp-from}")
    private String whatsappFrom;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS
                || channel == NotificationChannel.WHATSAPP;
    }

    @Override
    public void send(NotificationMessage message) {
        String body = message.getTemplateName();
        String to = message.getTo();
        String from;

        if (message.getChannel() == NotificationChannel.WHATSAPP) {
            from = whatsappFrom;
            to = "whatsapp:" + to;
        } else {
            from = fromPhone;
        }

        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(from),
                body
        ).create();
    }
}
