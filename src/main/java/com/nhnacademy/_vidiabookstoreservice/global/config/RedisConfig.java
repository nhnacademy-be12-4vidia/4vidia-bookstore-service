package com.nhnacademy._vidiabookstoreservice.global.config;

import com.nhnacademy._vidiabookstoreservice.cart.service.scheduler.CartExpireListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final CartExpireListener cartExpireListener;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("cartRedisConnectionFactory")
            RedisConnectionFactory cartRedisConnectionFactory
    ) {
        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();
        container.setConnectionFactory(cartRedisConnectionFactory);

        container.addMessageListener(
                cartExpireListener,
                new PatternTopic("__keyevent@*__:expired")
        );

        return container;
    }
}

