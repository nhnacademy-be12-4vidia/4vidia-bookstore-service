package com.nhnacademy._vidiabookstoreservice.global.config;

import com.nhnacademy._vidiabookstoreservice.global.interceptor.UserHeaderInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserHeaderInterceptor userHeaderInterceptor;

    public WebConfig(UserHeaderInterceptor userHeaderInterceptor) {
        this.userHeaderInterceptor = userHeaderInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userHeaderInterceptor)
                .addPathPatterns("/**"); // 모든 요청에 적용
    }
}
