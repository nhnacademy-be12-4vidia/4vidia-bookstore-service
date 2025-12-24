package com.nhnacademy._vidiabookstoreservice.book.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class BookCacheConfig {

    @Bean
    @Primary
    public CacheManager categoryListCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(100));
        return manager;
    }

    @Bean
    public CacheManager isbnSearchCacheManager(
            @Qualifier("isbnRedisConnectionFactory") RedisConnectionFactory isbnRedisConnectionFactory
    ) {
        // 기본 ObjectMapper는 클래스에 대한 정보를 모르기 때문에 ClassCastException 발생
        // 해결을 위해 다형성 타입 정보를 포함하도록 설정
        // BasicPolymorphicTypeValidator를 사용하여 허용된 패키지 설정
        ObjectMapper cacheObjectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("com.nhnacademy._vidiabookstoreservice")
                                .allowIfSubType("java.util")
                                .allowIfSubType("java.time")
                                .allowIfSubType("java.lang")
                                .build(),
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                );

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(cacheObjectMapper)))
                .prefixCacheNameWith("book:isbn:");

        return RedisCacheManager.builder(isbnRedisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
