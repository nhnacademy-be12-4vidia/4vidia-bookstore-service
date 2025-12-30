package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.nhnacademy._vidiabookstoreservice.book.exception.ImageCanNotDeleteException;
import com.nhnacademy._vidiabookstoreservice.book.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
            throw new ImageUploadException();
        }
    }

    public String createFileName(String fileName) {
        return UUID.randomUUID().toString() + "_" + fileName;
    }

    public void delete(String url) {
        String objectKey = extractKeyFromUrl(url);

        amazonS3.deleteObject(bucket, objectKey);
    }

    public boolean isMinioUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return false;
        try {
            URI uri = new URI(imageUrl);
            String path = uri.getPath();
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            String bucketPrefix = "/" + bucket + "/";
            return decodedPath.startsWith(bucketPrefix);
        } catch (Exception e) {
            return false;
        }
    }

    private String extractKeyFromUrl(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);

            String path = uri.getPath();
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            String bucketPrefix = "/" + bucket + "/";

            if (decodedPath.startsWith(bucketPrefix)) {
                return decodedPath.substring(bucketPrefix.length());
            } else {
                throw new IllegalArgumentException("해당 버킷의 파일이 아닙니다.");
            }
        } catch (Exception e) {
            throw new ImageCanNotDeleteException();
        }

    }

}
