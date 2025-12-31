package com.nhnacademy._vidiabookstoreservice.book.mq.producer;

import com.nhnacademy._vidiabookstoreservice.book.config.RabbitMQConfig;
import com.nhnacademy._vidiabookstoreservice.book.dto.event.DescriptionImageUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageEventProducer {

    private final RabbitTemplate rabbitTemplate;
    public static final String ROUTING_KEY_DESCRIPTION_IMAGE = "storage.image.uploaded.description";

    public void sendDescriptionImageUploadedEvent(DescriptionImageUploadedEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.STORAGE_EXCHANGE,
            ROUTING_KEY_DESCRIPTION_IMAGE,
            event
        );
        log.info("[Producer] 도서 설명 이미지 업로드 이벤트 발송: {}", event.imageUrl());
    }
}
