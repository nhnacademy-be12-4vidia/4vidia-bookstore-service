package com.nhnacademy._vidiabookstoreservice.book.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;


@Configuration
@RequiredArgsConstructor
public class GeminiConfig {

    private final GeminiProperties geminiProperties;

    @Bean
    public RestClient geminiRestClient() {
        return RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .requestInterceptor((request, body, execution) -> {
                String apiKey = resolveApiKey();
                if (StringUtils.hasText(apiKey)) {
                    request.getHeaders().set("x-goog-api-key", apiKey);
                }
                return execution.execute(request, body);
            })
            .build();
    }

    private String resolveApiKey() {
        List<String> keyList = geminiProperties.getApiKeyList();
        if (keyList != null && !keyList.isEmpty()) {
            int idx = ThreadLocalRandom.current().nextInt(keyList.size());
            String k = keyList.get(idx);
            if (StringUtils.hasText(k)) {
                return k;
            }
        }
        for (String candidate : keyList) {
            if (StringUtils.hasText(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}

