package com.nhnacademy._vidiabookstoreservice.book.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureOrder()
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "discount.exchange";
    public static final String QUEUE = "discount.policy.reprice.queue";
    public static final String ROUTING_KEY = "discount.policy.changed";

    public static final String STORAGE_EXCHANGE = "storage.exchange";

    @Bean
    public TopicExchange storageExchange() {
        return new TopicExchange(STORAGE_EXCHANGE);
    }

    @Bean
    public TopicExchange discountExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue discountPolicyQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding discountPolicyBinding() {
        return BindingBuilder
                .bind(discountPolicyQueue())
                .to(discountExchange())
                .with(ROUTING_KEY);
    }
}
