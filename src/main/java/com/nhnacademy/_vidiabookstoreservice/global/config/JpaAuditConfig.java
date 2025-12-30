package com.nhnacademy._vidiabookstoreservice.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
    // JPA Auditing 설정을 별도 클래스로 분리하여
    // @WebMvcTest 실행 시 로드되지 않도록 함
}