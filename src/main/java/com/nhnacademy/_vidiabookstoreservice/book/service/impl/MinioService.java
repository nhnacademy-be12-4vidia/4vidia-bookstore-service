package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.nhnacademy._vidiabookstoreservice.book.exception.ImageUploadException;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile uploadFile)  {

        String fileName = createFileName(uploadFile.getOriginalFilename());

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(uploadFile.getSize());
            metadata.setContentType(uploadFile.getContentType());

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucket,
                fileName,
                uploadFile.getInputStream(),
                metadata
            );

            amazonS3.putObject(putObjectRequest);

            return amazonS3.getUrl(bucket, fileName).toString();

        } catch (IOException e) {
            throw new ImageUploadException("이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    public String createFileName(String fileName) {
        return UUID.randomUUID().toString() + "_" + fileName;
    }

}
