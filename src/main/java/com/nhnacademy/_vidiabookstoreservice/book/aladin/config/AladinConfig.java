package com.nhnacademy._vidiabookstoreservice.book.aladin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class AladinConfig {

    @Bean
    public RestClient aladinRestClient() {
        int connectTimeoutMillis = 5_000;
        int readTimeoutMillis = 10_000;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMillis));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMillis));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
