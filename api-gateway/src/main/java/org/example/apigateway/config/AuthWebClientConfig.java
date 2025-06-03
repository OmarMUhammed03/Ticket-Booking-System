package org.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AuthWebClientConfig {

    @Value( "${routes.auth}")
    String authUrl;
    @Bean
    public WebClient authWebClient() {
        return WebClient.builder()
                .baseUrl(authUrl)
                .build();
    }
}