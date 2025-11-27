package com.nhnacademy._vidiabookstoreservice.book.service.impl;


import static org.assertj.core.api.Assertions.*;

import com.nhnacademy._vidiabookstoreservice.order.controller.PaymentController;
import com.nhnacademy._vidiabookstoreservice.order.service.impl.TossPaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@Slf4j
class MinioServiceTest {

    @Autowired
    private MinioService minioService;

    @MockitoBean
    private PaymentController paymentController;

    @MockitoBean
    private TossPaymentServiceImpl tossPaymentServiceImpl;

    @Test
    void uploadRealFile() {

        MultipartFile file = new MockMultipartFile(
            "image",
            "test-image.jpg",
            "image/jpeg",
            "Hello Minio".getBytes()
        );

        String url = minioService.upload(file);

        log.info("업로드 Url : {}", url);

        assertThat(url).isNotNull();
        assertThat(url).contains("test-image.jpg");
    }

}