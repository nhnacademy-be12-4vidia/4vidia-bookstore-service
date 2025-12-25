package com.nhnacademy._vidiabookstoreservice.global.config;

import com.nhnacademy._vidiabookstoreservice.global.interceptor.LogInterceptor;
import com.nhnacademy._vidiabookstoreservice.global.interceptor.UserHeaderInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LogInterceptor logInterceptor;
    private final UserHeaderInterceptor userHeaderInterceptor;

    public WebConfig(LogInterceptor logInterceptor, UserHeaderInterceptor userHeaderInterceptor) {
        this.logInterceptor = logInterceptor;
        this.userHeaderInterceptor = userHeaderInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .order(Ordered.HIGHEST_PRECEDENCE);

        registry.addInterceptor(userHeaderInterceptor)
                .addPathPatterns("/**") // 모든 요청에 적용
                .order(1);
    }
}
