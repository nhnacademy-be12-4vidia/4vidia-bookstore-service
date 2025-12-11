package com.nhnacademy._vidiabookstoreservice.book.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.api")
@Getter
@Setter
public class GeminiProperties {
    private String apiKey;
    private String model;   // default: gemini-2.5-flash
}
