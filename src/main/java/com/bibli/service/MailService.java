package com.bibli.service;

import com.bibli.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

/**
 * Service for sending emails asynchronously, through the Resend HTTPS API rather than SMTP.
 * <p>
 * Cloud hosts (Railway included) commonly block outbound SMTP ports (25/465/587) on free/trial
 * plans to prevent spam abuse, which made the previous JavaMailSender/SMTP-based implementation
 * time out in production. HTTPS isn't blocked, so we call Resend's REST API directly instead.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    private static final URI RESEND_API_URL = URI.create("https://api.resend.com/emails");

    private final JHipsterProperties jHipsterProperties;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    public MailService(
        JHipsterProperties jHipsterProperties,
        MessageSource messageSource,
        SpringTemplateEngine templateEngine,
        HttpClient httpClient
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.httpClient = httpClient;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        sendEmailSync(to, subject, content, isMultipart, isHtml);
    }

    private void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        LOG.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}'", isMultipart, isHtml, to, subject);

        if (resendApiKey == null || resendApiKey.isBlank()) {
            LOG.warn("RESEND_API_KEY is not configured, cannot send email to '{}'", to);
            return;
        }

        try {
            Map<String, Object> body = buildEmailPayload(to, subject, content, isHtml);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(RESEND_API_URL)
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                LOG.debug("Sent email to '{}'", to);
            } else {
                LOG.warn("Email could not be sent to '{}': Resend responded with HTTP {} - {}", to, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            LOG.warn("Email could not be sent to '{}'", to, e);
        }
    }

    /**
     * Builds the JSON payload sent to the Resend API. Package-private so it can be unit tested
     * without having to decode an {@link HttpRequest} body publisher.
     */
    Map<String, Object> buildEmailPayload(String to, String subject, String content, boolean isHtml) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", jHipsterProperties.getMail().getFrom());
        body.put("to", List.of(to));
        body.put("subject", subject);
        body.put(isHtml ? "html" : "text", content);
        return body;
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        sendEmailFromTemplateSync(user, templateName, titleKey);
    }

    private void sendEmailFromTemplateSync(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            LOG.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmailSync(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendActivationEmail(User user) {
        LOG.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplateSync(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(User user) {
        LOG.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplateSync(user, "mail/creationEmail", "email.activation.title");
    }

    @Async
    public void sendPasswordResetMail(User user) {
        LOG.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplateSync(user, "mail/passwordResetEmail", "email.reset.title");
    }
}
