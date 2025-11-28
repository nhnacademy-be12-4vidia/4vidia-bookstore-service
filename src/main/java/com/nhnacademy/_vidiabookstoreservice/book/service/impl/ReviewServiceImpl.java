package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.domain.ReviewImage;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.ReviewUserMismatchException;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewImageService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final OrderItemService orderItemService;
    private final BookService bookService;
    private final MinioService minioService;
    private final ReviewImageService reviewImageService;


    @Override
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> getReviewListByBookId(Long bookId, Long userId, Pageable pageable) {

        Page<Review> pageReview = reviewRepository.findByBookId(bookId, pageable);

        return pageReview.map(r -> ReviewListResponse.of(r, userId));
    }

    @Override
    @Transactional
    public void createReview(ReviewCreateRequest request, Long userId, List<MultipartFile> reviewImages) {

        OrderItemResponse orderItemResponse = orderItemService.getByOrderItemId(request.getOrderItemId());
        Long orderedUserId = orderItemResponse.order().getUserId();

        if (!orderedUserId.equals(userId)) {
            throw new ReviewUserMismatchException(
                "해당 상품을 주문한 사용자만 리뷰를 작성할 수 있습니다. 작성자 아이디 : %d, 주문자 아이디 : %d".formatted(userId,
                    orderedUserId));
        }
        OrderItem orderItemProxy = orderItemService.getProxyById(orderItemResponse.orderItemId());
        Book reviewedBookProxy = bookService.getProxyById(orderItemResponse.bookId());
        User userProxy = userService.getProxyById(userId);


        Review review = request.toEntity(orderItemProxy, reviewedBookProxy, userProxy, false);

        Review savedReview = reviewRepository.save(review);

        if (reviewImages != null && !reviewImages.isEmpty()) {
            saveReviewImages(reviewImages, savedReview);
        }

        savedReview.updateHasPhotoStatus();
    }

    @Transactional
    public void saveReviewImages(List<MultipartFile> reviewImages, Review review) {
        if (reviewImages == null || reviewImages.isEmpty()) return;

        Set<String> uniqueFileCheck = new HashSet<>();

        List<ReviewImage> reviewImageList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        int order = 0;

        for (MultipartFile file : reviewImages) {
            if (file.isEmpty()) continue;

            sb.setLength(0);

            String duplicateKey = sb.append(file.getOriginalFilename()).append("_")
                .append(file.getSize()).toString();

            if (!uniqueFileCheck.add(duplicateKey)) {
                continue;
            }

            String imageUrl = minioService.upload(file);

            ReviewImage reviewImage = ReviewImage.builder()
                .imageUrl(imageUrl)
                .review(review)
                .displayOrder(order++)
                .build();

            reviewImageList.add(reviewImage);
            review.addReviewImage(reviewImage);
        }
        reviewImageService.saveAll(reviewImageList);
    }
}
