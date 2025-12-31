package com.nhnacademy._vidiabookstoreservice.book.mq.producer;

import com.nhnacademy._vidiabookstoreservice.book.dto.event.DescriptionImageUploadedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageEventProducerTest {

    @InjectMocks
    private StorageEventProducer storageEventProducer;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("이벤트 발송 성공 - 지정된 Exchange와 RoutingKey로 메시지가 전송된다")
    void sendDescriptionImageUploadedEvent_Success() {
        String imageUrl = "http://test-image.com/1.jpg";
        LocalDateTime uploadedAt = LocalDateTime.now();
        DescriptionImageUploadedEvent event = new DescriptionImageUploadedEvent(imageUrl, uploadedAt);

        String expectedExchange = "storage.exchange";
        String expectedRoutingKey = "storage.image.uploaded.description";

        storageEventProducer.sendDescriptionImageUploadedEvent(event);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(expectedExchange),
                eq(expectedRoutingKey),
                eq(event)
        );
    }

}