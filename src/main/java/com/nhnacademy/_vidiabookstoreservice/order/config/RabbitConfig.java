package com.nhnacademy._vidiabookstoreservice.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureOrder(0)
@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jacksonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }

    // --- 실제 처리 ---
    @Bean
    public DirectExchange processingExchange() {
        return new DirectExchange("processing.exchange");
    }

    @Bean
    public Queue processingQueue() {
        return new Queue("processing.queue");
    }

    @Bean
    public Binding processingBinding() {
        return BindingBuilder.bind(processingQueue())
                .to(processingExchange())
                .with("order.process");
    }

    // --- TTL + DLX 전략: 지연큐 처럼 보이게 만듦 ---
    @Bean
    public DirectExchange waitingExchange() {
        return new DirectExchange("waiting.exchange");
    }

    @Bean
    public Queue waitingQueue() {
        return QueueBuilder.durable("waiting.queue")
                .ttl(15 * 60 * 1000) // 15분 뒤 dead
                .deadLetterExchange("processing.exchange")
                .deadLetterRoutingKey("order.process")
                .build();
    }

    @Bean
    public Binding waitingBinding() {
        return BindingBuilder.bind(waitingQueue())
                .to(waitingExchange())
                .with("order.wait");
    }
}
