package com.nhnacademy._vidiabookstoreservice.book.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@ConditionalOnProperty(name = "spring.data.elasticsearch.repositories.enabled", havingValue = "true")
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.nhnacademy._vidiabookstoreservice.book.repository.search")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String esUrl;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
            .connectedTo(esUrl)
            .withBasicAuth(username, password)
            .withSocketTimeout(30000)
            .build();
    }
}
