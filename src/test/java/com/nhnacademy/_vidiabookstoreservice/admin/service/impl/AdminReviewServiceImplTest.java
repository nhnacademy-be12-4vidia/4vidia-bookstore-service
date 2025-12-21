package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminReviewResponse;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewRepository;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private AdminReviewServiceImpl adminReviewService;

    @Test
    @DisplayName("리뷰 목록 조회 - DTO 변환 및 연관 데이터 검증")
    void getReviews_Success() {
        // 1. Given - 테스트 데이터 설정
        String keyword = "추천";
        Integer rating = 5;
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 연관 엔티티 생성 (Response에서 사용하는 필드 위주로 설정)
        User mockUser = mock(User.class);
        given(mockUser.getEmail()).willReturn("test@nhn.com");
        given(mockUser.getName()).willReturn("테스트");

        Book mockBook = mock(Book.class);
        ReflectionTestUtils.setField(mockBook, "id", 50L); // Book ID 주입
        given(mockBook.getTitle()).willReturn("자바 테스트 가이드");

        OrderItem mockOrderItem = mock(OrderItem.class);

        // Review 객체 빌더 생성
        Review review = Review.builder()
                .user(mockUser)
                .book(mockBook)
                .orderItem(mockOrderItem)
                .rating(rating)
                .content("내용입니다.")
                .hasPhoto(false)
                .build();

        // 필드 주입 (id, createdAt, imageList)
        ReflectionTestUtils.setField(review, "id", 100L);
        ReflectionTestUtils.setField(review, "createdAt", LocalDate.now());
        ReflectionTestUtils.setField(review, "imageList", new ArrayList<>()); // 이미지 리스트 초기화

        Page<Review> reviewPage = new PageImpl<>(List.of(review), pageable, 1);
        given(reviewRepository.searchAdminReviews(keyword, rating, pageable)).willReturn(reviewPage);

        // 2. When
        PageResponse<AdminReviewResponse> result = adminReviewService.getReviews(keyword, rating, page, size);

        // 3. Then
        assertThat(result.content()).hasSize(1);
        AdminReviewResponse response = result.content().get(0);

        // Response 매핑 값 검증
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.bookTitle()).isEqualTo("자바 테스트 가이드");
        assertThat(response.email()).isEqualTo("test@nhn.com");
        assertThat(response.userNickname()).isEqualTo("테스트");
        assertThat(response.createdAt()).isNotNull();

        verify(reviewRepository).searchAdminReviews(keyword, rating, pageable);
    }
}