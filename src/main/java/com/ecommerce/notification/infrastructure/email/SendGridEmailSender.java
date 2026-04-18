package com.ecommerce.notification.infrastructure.email;

import com.ecommerce.notification.application.port.NotificationChannel;
import com.ecommerce.notification.application.port.NotificationMessage;
import com.ecommerce.notification.application.port.NotificationSender;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendGridEmailSender implements NotificationSender {

    private final SendGrid sendGrid;
    private final TemplateEngine templateEngine;

    @Value("${notification.sendgrid.from-email}")
    private String fromEmail;

    @Value("${notification.sendgrid.from-name}")
    private String fromName;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationMessage message) {
        // 1. Render Thymeleaf template
        Context context = new Context();
        context.setVariables(message.getVariables());
        String htmlBody = templateEngine.process(message.getTemplateName(), context);

        // 2. Build SendGrid mail object
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(message.getTo());
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, message.getSubject(), to, content);

        // 3. Send via API
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            var response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                throw new RuntimeException(
                        "SendGrid error " + response.getStatusCode()
                        + ": " + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to send email via SendGrid", e);
        }
    }
}