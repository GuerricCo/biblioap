package com.bibli.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bibli.IntegrationTest;
import com.bibli.config.Constants;
import com.bibli.domain.User;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import tech.jhipster.config.JHipsterProperties;

/**
 * Integration tests for {@link MailService}. Emails are sent through the Resend HTTPS API rather
 * than SMTP (see {@link MailService} for why), so the HTTP client is mocked instead of a
 * JavaMailSender.
 */
@ExtendWith(MockitoExtension.class)
@IntegrationTest
class MailServiceIT {

    @Autowired
    private JHipsterProperties jHipsterProperties;

    @MockitoBean
    private HttpClient httpClient;

    @Autowired
    private MailService mailService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() throws Exception {
        ReflectionTestUtils.setField(mailService, "resendApiKey", "re_test_key");
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
    }

    @Test
    void buildsPlainTextPayload() {
        Map<String, Object> payload = mailService.buildEmailPayload("john.doe@example.com", "testSubject", "testContent", false);
        assertThat(payload.get("from")).isEqualTo(jHipsterProperties.getMail().getFrom());
        assertThat(payload.get("to")).isEqualTo(List.of("john.doe@example.com"));
        assertThat(payload.get("subject")).isEqualTo("testSubject");
        assertThat(payload.get("text")).isEqualTo("testContent");
        assertThat(payload).doesNotContainKey("html");
    }

    @Test
    void buildsHtmlPayload() {
        Map<String, Object> payload = mailService.buildEmailPayload("john.doe@example.com", "testSubject", "testContent", true);
        assertThat(payload.get("html")).isEqualTo("testContent");
        assertThat(payload).doesNotContainKey("text");
    }

    @Test
    void testSendEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false);
        verify(httpClient, timeout(1000)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testSendEmailUsesResendAuthorization() throws Exception {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false);
        var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, timeout(1000)).send(captor.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest request = captor.getValue();
        assertThat(request.uri().toString()).isEqualTo("https://api.resend.com/emails");
        assertThat(request.headers().firstValue("Authorization")).contains("Bearer re_test_key");
    }

    @Test
    void doesNotCallResendWhenApiKeyIsBlank() throws Exception {
        ReflectionTestUtils.setField(mailService, "resendApiKey", "");
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false);
        Thread.sleep(200);
        verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void doesNotThrowWhenResendCallFails() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new java.io.IOException("boom"));
        assertThatCode(() ->
            mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false)
        ).doesNotThrowAnyException();
    }

    @Test
    void testSendEmailFromTemplate() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        mailService.sendEmailFromTemplate(user, "mail/testEmail", "email.test.title");
        verify(httpClient, timeout(1000)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testSendActivationEmail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        mailService.sendActivationEmail(user);
        verify(httpClient, timeout(1000)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testCreationEmail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        mailService.sendCreationEmail(user);
        verify(httpClient, timeout(1000)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testSendPasswordResetMail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        mailService.sendPasswordResetMail(user);
        verify(httpClient, timeout(1000)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void doesNotSendEmailWhenUserHasNoAddress() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        mailService.sendActivationEmail(user);
        Thread.sleep(200);
        verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}
