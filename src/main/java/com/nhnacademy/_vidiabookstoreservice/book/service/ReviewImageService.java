package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.domain.ReviewImage;
import java.util.List;

public interface ReviewImageService {

    ReviewImage create(Review review, String imageUrl, Integer displayOrder);

    void saveAll(List<ReviewImage> reviewImages);

    ReviewImage createByEntity(ReviewImage reviewImage);

}
