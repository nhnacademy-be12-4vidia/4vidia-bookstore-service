package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookReviewSummary;
import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.domain.ReviewImage;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.ReviewUserMismatchException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.ReviewNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookReviewSummaryRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewImageService;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private UserService userService;
    @Mock private OrderItemService orderItemService;
    @Mock private BookService bookService;
    @Mock private MinioService minioService;
    @Mock private ReviewImageService reviewImageService;
    @Mock private PointCommandService pointCommandService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private BookReviewSummaryRepository bookReviewSummaryRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    @DisplayName("리뷰 목록 조회 - 페이징 확인")
    void getReviewListByBookId_success() {
        Long bookId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);
        Review review = mock(Review.class);
        User user = mock(User.class);
        when(review.getUser()).thenReturn(user);
        when(reviewRepository.findByBookId(bookId, pageable)).thenReturn(new PageImpl<>(List.of(review)));

        Page<ReviewListResponse> result = reviewService.getReviewListByBookId(bookId, 1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(reviewRepository).findByBookId(bookId, pageable);
    }

    @Test
    @DisplayName("리뷰 생성 - 이미지 포함 시 MinIO 업로드 및 사진 리뷰 포인트 적립 (3번 정책)")
    void createReview_withImages_success() {
        Long userId = 1L;
        Long bookId = 100L;
        Long orderItemId = 10L;

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);
        when(request.getOrderItemId()).thenReturn(orderItemId);
        when(request.getBookId()).thenReturn(bookId);

        OrderItemResponse orderItemResponse = mock(OrderItemResponse.class);
        com.nhnacademy._vidiabookstoreservice.order.domain.Order orderEntity = mock(com.nhnacademy._vidiabookstoreservice.order.domain.Order.class);
        com.nhnacademy._vidiabookstoreservice.user.domain.User userEntity = mock(com.nhnacademy._vidiabookstoreservice.user.domain.User.class);

        when(orderItemService.getByOrderItemId(orderItemId)).thenReturn(orderItemResponse);

        when(orderItemResponse.bookId()).thenReturn(bookId);
        when(orderItemResponse.orderItemId()).thenReturn(orderItemId);

        when(orderItemResponse.order()).thenReturn(orderEntity);
        when(orderEntity.getUser()).thenReturn(userEntity);
        when(userEntity.getUserId()).thenReturn(userId);

        when(orderItemService.getProxyById(orderItemId)).thenReturn(mock(com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem.class));
        when(bookService.getProxyById(bookId)).thenReturn(mock(Book.class));
        when(userService.getProxyById(userId)).thenReturn(mock(com.nhnacademy._vidiabookstoreservice.user.domain.User.class));

        Review reviewEntity = mock(Review.class);
        when(request.toEntity(any(), any(), any(), eq(false))).thenReturn(reviewEntity);
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewEntity);

        when(reviewEntity.isHasPhoto()).thenReturn(true);

        MultipartFile imageFile = mock(MultipartFile.class);
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(imageFile.getSize()).thenReturn(1024L);
        when(minioService.upload(any(MultipartFile.class))).thenReturn("http://minio/photo.jpg");

        when(bookReviewSummaryRepository.findById(bookId)).thenReturn(Optional.of(mock(BookReviewSummary.class)));

        reviewService.createReview(request, userId, List.of(imageFile));

        verify(minioService).upload(any(MultipartFile.class));
        verify(pointCommandService).rewardByPolicy(argThat(r -> r.userId().equals(userId) && r.policyId() == 3L));
        verify(reviewImageService).saveAll(anyList());
    }

    @Test
    @DisplayName("리뷰 생성 - 텍스트만 있는 리뷰 시 2번 정책 포인트 적립")
    void createReview_textOnly_success() {
        Long userId = 1L;
        Long bookId = 100L;
        ReviewCreateRequest request = mock(ReviewCreateRequest.class);
        when(request.getOrderItemId()).thenReturn(10L);
        when(request.getBookId()).thenReturn(bookId);

        OrderItemResponse orderItemResponse = mock(OrderItemResponse.class);
        com.nhnacademy._vidiabookstoreservice.order.domain.Order order = mock(com.nhnacademy._vidiabookstoreservice.order.domain.Order.class);
        User user = mock(User.class);

        when(orderItemService.getByOrderItemId(10L)).thenReturn(orderItemResponse);
        when(orderItemResponse.order()).thenReturn(order);
        when(order.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        Review reviewEntity = mock(Review.class);
        when(request.toEntity(any(), any(), any(), eq(false))).thenReturn(reviewEntity);
        when(reviewRepository.save(any())).thenReturn(reviewEntity);
        when(reviewEntity.isHasPhoto()).thenReturn(false); // 사진 없음

        when(bookReviewSummaryRepository.findById(bookId)).thenReturn(Optional.empty());
        when(bookReviewSummaryRepository.save(any())).thenReturn(mock(BookReviewSummary.class));

        reviewService.createReview(request, userId, null);

        verify(pointCommandService).rewardByPolicy(argThat(r -> r.policyId() == 2L));
        verify(bookReviewSummaryRepository).save(any()); // 신규 요약 생성 확인
    }

    @Test
    @DisplayName("리뷰 생성 - 사용자 불일치 시 예외 발생")
    void createReview_userMismatch_throwsException() {
        Long userId = 1L;
        OrderItemResponse res = mock(OrderItemResponse.class);
        com.nhnacademy._vidiabookstoreservice.order.domain.Order order = mock(com.nhnacademy._vidiabookstoreservice.order.domain.Order.class);
        User user = mock(User.class);

        when(orderItemService.getByOrderItemId(any())).thenReturn(res);
        when(res.order()).thenReturn(order);
        when(order.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(2L); // 다른 사용자

        ReviewCreateRequest req = mock(ReviewCreateRequest.class);
        assertThrows(ReviewUserMismatchException.class, () -> reviewService.createReview(req, userId, null));
    }

    @Test
    @DisplayName("리뷰 수정 - 기존 이미지 삭제 및 신규 이미지 업로드")
    void updateReview_withNewImages_success() {
        Long userId = 1L;
        Long reviewId = 10L;
        Long bookId = 100L;
        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        Review review = mock(Review.class);
        User user = mock(User.class);
        Book book = mock(Book.class);
        ReviewImage oldImage = mock(ReviewImage.class);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(review.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);
        when(review.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(bookId);
        when(review.getImageList()).thenReturn(List.of(oldImage));
        when(oldImage.getImageUrl()).thenReturn("old-url");

        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false);
        when(minioService.upload(newFile)).thenReturn("new-url");

        reviewService.updateReview(userId, reviewId, bookId, request, List.of(newFile));

        verify(minioService).delete("old-url"); // 기존 이미지 삭제 확인
        verify(review).cleanImageList(); // 리스트 초기화 확인
        verify(minioService).upload(newFile); // 새 이미지 업로드 확인
    }

    @Test
    @DisplayName("리뷰 수정 - 이미지 리스트가 null인 경우 본문만 수정")
    void updateReview_noImageUpdate_success() {
        Long userId = 1L;
        Long reviewId = 10L;
        Long bookId = 100L;

        Review review = mock(Review.class);
        User user = mock(User.class);
        Book book = mock(Book.class);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(review.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);
        when(review.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(bookId);

        reviewService.updateReview(userId, reviewId, bookId, mock(ReviewUpdateRequest.class), null);

        verify(review).updateContent(any());
        verify(minioService, never()).delete(any());
    }

    @Test
    @DisplayName("리뷰 수정 - 도서 ID 불일치 시 수정 불가 예외")
    void updateReview_bookIdMismatch_throwsException() {
        Long userId = 1L;
        Review review = mock(Review.class);
        User user = mock(User.class);
        Book book = mock(Book.class);

        when(reviewRepository.findById(any())).thenReturn(Optional.of(review));
        when(review.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);
        when(review.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(200L); // 실제는 200L인데 요청은 100L

        ReviewUpdateRequest req = mock(ReviewUpdateRequest.class);
        assertThrows(AccessDeniedException.class, () -> reviewService.updateReview(userId, 1L, 100L, req, null));
    }

    @Test
    @DisplayName("단순 조회 메서드들 커버리지 (Count, Avg, List)")
    void simpleMethods_coverage() {
        when(reviewRepository.findReviewedOrderItemIdList(any())).thenReturn(List.of(1L));
        when(reviewRepository.countByBook_Id(1L)).thenReturn(Optional.of(5L));
        when(reviewRepository.findAverageRatingByBookId(1L)).thenReturn(4.5);

        assertEquals(1, reviewService.getReviewedOrderItemIdList(List.of(1L)).size());
        assertEquals(5L, reviewService.getTotalReviewCount(1L));
        assertEquals(4.5, reviewService.getAvgReviewRating(1L));
    }

    @Test
    @DisplayName("getTotalReviewCount - 데이터 없을 시 0 반환")
    void getTotalReviewCount_empty_returnsZero() {
        when(reviewRepository.countByBook_Id(1L)).thenReturn(Optional.empty());
        assertEquals(0L, reviewService.getTotalReviewCount(1L));
    }

    @Test
    @DisplayName("saveReviewImages - 비어있는 이미지 리스트 처리")
    void saveReviewImages_empty_returnsImmediately() {
        reviewService.saveReviewImages(null, mock(Review.class));
        reviewService.saveReviewImages(new ArrayList<>(), mock(Review.class));
        verify(minioService, never()).upload(any());
    }

    @Test
    @DisplayName("deactivateReview - 삭제 결과 없을 시 예외 발생")
    void deactivateReview_noUpdate_throwsException() {
        when(reviewRepository.deactivateReview(any(), any(), any())).thenReturn(0);
        assertThrows(AccessDeniedException.class, () -> reviewService.deactivateReview(1L, 1L, 1L));
    }

    @Test
    @DisplayName("updateReview - 리뷰를 찾을 수 없을 때 예외 발생")
    void updateReview_notFound_throwsException() {
        when(reviewRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ReviewNotFoundException.class, () -> reviewService.updateReview(1L, 1L, 1L, null, null));
    }

    @Test
    @DisplayName("비활성화(삭제) - 성공")
    void deactivateReview_success() {
        when(reviewRepository.deactivateReview(1L, 1L, 1L)).thenReturn(1);

        assertDoesNotThrow(() -> reviewService.deactivateReview(1L, 1L, 1L));

        verify(reviewRepository).deactivateReview(1L, 1L, 1L);
    }

    @Test
    @DisplayName("리뷰 요약용 상위 200개 조회 - 데이터 없을 시 빈 리스트 반환")
    void getTop200ReviewListByBookId_empty_returnsEmptyList() {
        when(reviewRepository.findTop200ByBook_IdOrderByIdDesc(1L)).thenReturn(new ArrayList<>());

        List<Review> result = reviewService.getTop200ReviewListByBookId(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("saveReviewImages - 동일한 파일명/사이즈 중복 업로드 방지")
    void saveReviewImages_duplicatePrevention() {
        Review review = mock(Review.class);
        MultipartFile file1 = mock(MultipartFile.class);
        when(file1.isEmpty()).thenReturn(false);
        when(file1.getOriginalFilename()).thenReturn("same.jpg");
        when(file1.getSize()).thenReturn(100L);
        when(minioService.upload(any())).thenReturn("url");

        reviewService.saveReviewImages(List.of(file1, file1), review);

        verify(minioService, times(1)).upload(any());
    }
}