package com.nhnacademy._vidiabookstoreservice.global.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.data.redis")
public class MultiRedisProperties {

    private RedisNode cart;
    private RedisNode dormant;
    private RedisNode bestseller;


    @Getter
    @Setter
    public static class RedisNode {
        private String host;
        private int port;
        private String password;
        private int database;
        private String timeout;
    }
}

