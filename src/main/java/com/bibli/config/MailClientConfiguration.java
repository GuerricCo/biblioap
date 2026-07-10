package com.bibli.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The {@link HttpClient} used by {@link com.bibli.service.MailService} to call the Resend API.
 * Declared as a bean (rather than a plain field) so it can be substituted with a mock in tests.
 */
@Configuration
public class MailClientConfiguration {

    @Bean
    public HttpClient mailHttpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }
}
