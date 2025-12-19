package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.domain.ReviewImage;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.ImageAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewImageRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewImageService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewImageServiceImpl implements ReviewImageService {

    private final ReviewImageRepository reviewImageRepository;

    @Override
    @Transactional
    public ReviewImage create(Review review, String imageUrl, Integer displayOrder) {

        ReviewImage reviewImage = ReviewImage.builder()
            .review(review)
            .displayOrder(displayOrder)
            .imageUrl(imageUrl)
            .build();

        return createByEntity(reviewImage);

    }

    @Override
    @Transactional
    public void saveAll(List<ReviewImage> reviewImages) {
        if(reviewImages.isEmpty()) return;

        List<String> requestUrlList = reviewImages.stream().map(ReviewImage::getImageUrl).toList();

        Set<String> existingUrls = reviewImageRepository.findExistingUrls(requestUrlList);

        List<ReviewImage> reviewImageList = reviewImages.stream()
            .filter(ri -> !existingUrls.contains(ri.getImageUrl())).toList();

        if (!reviewImageList.isEmpty()) {
            reviewImageRepository.saveAll(reviewImageList);
        }

    }

    @Override
    public ReviewImage createByEntity(ReviewImage reviewImage) {

        if (reviewImageRepository.existsByReview_IdAndImageUrl(reviewImage.getId(),
            reviewImage.getImageUrl())) {
            throw new ImageAlreadyExistsException(reviewImage.getReview().getId(), reviewImage.getImageUrl());
        }
        return reviewImageRepository.save(reviewImage);
    }

}
