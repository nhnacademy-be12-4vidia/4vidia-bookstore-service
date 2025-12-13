package com.nhnacademy._vidiabookstoreservice.global.config;

import com.nhnacademy._vidiabookstoreservice.global.common.MultiRedisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(MultiRedisProperties.class)
@RequiredArgsConstructor
public class MultiRedisConfig {

    private final MultiRedisProperties props;

    @Bean
    @Primary // 장바구니 + 선택된 주문아이템
    public LettuceConnectionFactory cartRedisConnectionFactory() {
        MultiRedisProperties.RedisNode c = props.getCart();
        if (c == null) {
            throw new IllegalStateException("data.redis.cart 설정이 없습니다.");
        }

        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(c.getHost(), c.getPort());
        config.setDatabase(c.getDatabase());
        if (c.getPassword() != null && !c.getPassword().isBlank()) {
            config.setPassword(RedisPassword.of(c.getPassword()));
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public StringRedisTemplate cartRedisTemplate(
            @Qualifier("cartRedisConnectionFactory") LettuceConnectionFactory cf
    ) {
        return new StringRedisTemplate(cf);
    }

    @Bean
    public RedisTemplate<String, Object> orderRedisTemplate( // value에 Object 저장위해
            @Qualifier("cartRedisConnectionFactory") LettuceConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean
    public LettuceConnectionFactory humanRedisConnectionFactory() {
        MultiRedisProperties.RedisNode h = props.getDormant();
        if (h == null) {
            throw new IllegalStateException("data.redis.dormant 설정이 없습니다.");
        }

        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(h.getHost(), h.getPort());
        config.setDatabase(h.getDatabase());
        if (h.getPassword() != null && !h.getPassword().isBlank()) {
            config.setPassword(RedisPassword.of(h.getPassword()));
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public StringRedisTemplate humanRedisTemplate(
            @Qualifier("humanRedisConnectionFactory") LettuceConnectionFactory cf
    ) {
        return new StringRedisTemplate(cf);
    }



    /**
     * 베스트셀러 저장용
     * */
    @Bean
    public LettuceConnectionFactory bestsellerRedisConnectionFactory() {
        MultiRedisProperties.RedisNode h = props.getBestseller();
        if (h == null) {
            throw new IllegalStateException("data.redis.bestseller 설정이 없습니다.");
        }

        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(h.getHost(), h.getPort());
        config.setDatabase(h.getDatabase());
        if (h.getPassword() != null && !h.getPassword().isBlank()) {
            config.setPassword(RedisPassword.of(h.getPassword()));
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public StringRedisTemplate bestsellerRedisTemplate(
            @Qualifier("bestsellerRedisConnectionFactory") LettuceConnectionFactory cf
    ) {
        return new StringRedisTemplate(cf);
    }
}
