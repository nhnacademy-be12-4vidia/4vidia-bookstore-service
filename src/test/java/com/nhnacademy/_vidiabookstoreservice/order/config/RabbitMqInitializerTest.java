package com.nhnacademy._vidiabookstoreservice.order.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMqInitializerTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    @InjectMocks
    private RabbitMqInitializer rabbitMqInitializer;

    @Test
    @DisplayName("초기화 메서드 실행 시 rabbitAdmin.initialize()가 1회 호출되는가?")
    void init_Success() {
        rabbitMqInitializer.init();

        verify(rabbitAdmin, times(1)).initialize();
    }
}