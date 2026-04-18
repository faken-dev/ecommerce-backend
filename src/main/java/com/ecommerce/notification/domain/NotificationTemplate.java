package com.ecommerce.notification.domain;

public enum NotificationTemplate {

    OTP_EMAIL(
        "otp-email",
        "Your verification code"
    ),
    WELCOME_EMAIL(
        "welcome-email",
        "Welcome to ECommerce!"
    ),
    OTP_SMS(
        "Your verification code is: {code}. Valid for {minutes} minutes.",
        null
    ),
    PASSWORD_CHANGED_EMAIL(
        "password-changed-email",
        "Password changed successfully"
    );

    private final String templateNameOrBody;
    private final String subject;

    NotificationTemplate(String templateNameOrBody, String subject) {
        this.templateNameOrBody = templateNameOrBody;
        this.subject = subject;
    }

    public String getTemplateName() { return templateNameOrBody; }
    public String getSubject()      { return subject; }
}
