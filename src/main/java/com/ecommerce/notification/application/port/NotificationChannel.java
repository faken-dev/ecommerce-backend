package com.ecommerce.notification.application.port;

import com.ecommerce.auth.domain.entity.OtpToken;

public enum NotificationChannel {
    EMAIL, SMS, WHATSAPP;

    public static NotificationChannel from(OtpToken.Channel channel) {
        return switch (channel) {
            case EMAIL    -> EMAIL;
            case SMS      -> SMS;
            case WHATSAPP -> WHATSAPP;
        };
    }
}
