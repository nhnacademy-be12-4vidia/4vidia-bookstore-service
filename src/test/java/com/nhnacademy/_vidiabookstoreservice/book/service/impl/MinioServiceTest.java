package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.nhnacademy._vidiabookstoreservice.book.exception.ImageCanNotDeleteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {
    @Mock
    private AmazonS3 amazonS3;
    @InjectMocks
    private MinioService minioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioService, "bucket", "test-bucket");
    }

    @Test
    @DisplayName("upload - 성공")
    void upload_success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getSize()).thenReturn(10L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(amazonS3.getUrl(any(), any())).thenReturn(new URL("http://minio/test-bucket/uuid_test.jpg"));

        String url = minioService.upload(file);

        assertNotNull(url);
        verify(amazonS3).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("isMinioUrl - 버킷명 포함 여부 확인")
    void isMinioUrl_check() {
        assertTrue(minioService.isMinioUrl("http://minio/test-bucket/file.jpg"));
        assertFalse(minioService.isMinioUrl("http://external/other-bucket/file.jpg"));
        assertFalse(minioService.isMinioUrl(null));
    }

    @Test
    @DisplayName("delete - 잘못된 URL일 경우 예외 발생")
    void delete_invalidUrl_throwsException() {
        assertThrows(ImageCanNotDeleteException.class, () -> minioService.delete("invalid-url"));
    }
}