package com.nhnacademy._vidiabookstoreservice.book.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "google.api")
@Getter
@Setter
public class GeminiProperties {
    private List<String> apiKeyList;
    private String model;   // default: gemini-2.5-flash
}
