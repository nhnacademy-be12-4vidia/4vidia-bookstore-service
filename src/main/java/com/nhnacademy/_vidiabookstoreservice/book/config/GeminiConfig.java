package com.nhnacademy._vidiabookstoreservice.book.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


@Configuration
@RequiredArgsConstructor
public class GeminiConfig {

    private final GeminiProperties geminiProperties;
    private final AtomicInteger keyCursor = new AtomicInteger(0);

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
        try {
            List<String> keys = geminiProperties.getApiKeyList();
            if (keys != null && !keys.isEmpty()) {
                int i = Math.floorMod(keyCursor.getAndIncrement(), keys.size());
                return keys.get(i);
            }
        } catch (NoSuchMethodError | Exception ignored) {

        }
        return Objects.requireNonNull(geminiProperties.getApiKeyList()).getFirst();
    }
}

