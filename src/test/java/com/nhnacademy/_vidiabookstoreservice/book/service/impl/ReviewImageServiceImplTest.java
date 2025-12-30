package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.domain.ReviewImage;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.ImageAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewImageRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewImageServiceImplTest {

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @InjectMocks
    private ReviewImageServiceImpl reviewImageService;

    @Test
    @DisplayName("리뷰 이미지 생성 - 성공")
    void create_success() {
        Review review = mock(Review.class);
        String imageUrl = "http://minio.com/review/img1.jpg";
        Integer displayOrder = 1;

        when(reviewImageRepository.existsByReview_IdAndImageUrl(any(), eq(imageUrl))).thenReturn(false);
        when(reviewImageRepository.save(any(ReviewImage.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewImage result = reviewImageService.create(review, imageUrl, displayOrder);

        assertNotNull(result);
        assertEquals(imageUrl, result.getImageUrl());
        assertEquals(displayOrder, result.getDisplayOrder());
        verify(reviewImageRepository).save(any(ReviewImage.class));
    }

    @Test
    @DisplayName("리뷰 이미지 생성 - 이미 존재하는 URL일 경우 예외 발생")
    void create_alreadyExists_throwsException() {
        Review review = mock(Review.class);
        String imageUrl = "duplicate.jpg";

        when(review.getId()).thenReturn(1L);
        when(reviewImageRepository.existsByReview_IdAndImageUrl(any(), eq(imageUrl))).thenReturn(true);

        assertThrows(ImageAlreadyExistsException.class, () ->
                reviewImageService.create(review, imageUrl, 1));
    }

    @Test
    @DisplayName("여러 이미지 저장 - 중복 제거 후 저장")
    void saveAll_filtersDuplicates() {
        ReviewImage img1 = ReviewImage.builder().imageUrl("url1").build();
        ReviewImage img2 = ReviewImage.builder().imageUrl("url2").build();
        List<ReviewImage> images = List.of(img1, img2);

        when(reviewImageRepository.findExistingUrls(anyList())).thenReturn(Set.of("url1"));

        reviewImageService.saveAll(images);

        verify(reviewImageRepository).saveAll(argThat(list -> {
            List<ReviewImage> result = (List<ReviewImage>) list;
            return result.size() == 1 && result.get(0).getImageUrl().equals("url2");
        }));
    }

    @Test
    @DisplayName("엔티티로 생성 - 성공")
    void createByEntity_success() {
        ReviewImage reviewImage = mock(ReviewImage.class);
        when(reviewImage.getId()).thenReturn(100L);
        when(reviewImage.getImageUrl()).thenReturn("test-url");

        when(reviewImageRepository.existsByReview_IdAndImageUrl(100L, "test-url")).thenReturn(false);
        when(reviewImageRepository.save(reviewImage)).thenReturn(reviewImage);

        ReviewImage result = reviewImageService.createByEntity(reviewImage);

        assertEquals(reviewImage, result);
        verify(reviewImageRepository).save(reviewImage);
    }
}