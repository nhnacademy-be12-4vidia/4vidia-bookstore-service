package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.book.service.BookReviewSummaryService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest extends SupportControllerTest {

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private BookReviewSummaryService bookReviewSummaryService;

    @Test
    @DisplayName("[리뷰 생성]")
    void createReview() throws Exception {
        // Given
        Long userId = 1L;
        Long bookId = 1L;

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookId(bookId);
        request.setOrderItemId(100L);
        request.setRating(5);
        request.setContent("정말 좋은 책입니다!");

        // When & Then
        mockMvc.perform(post("/books/{bookId}/reviews", bookId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE) // 컨트롤러 요구사항 유지
                        .content(objectMapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andDo(document("book-review-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("bookId").description("도서 ID (URL 경로)")
                        ),
                        // requestParameters 대신 requestFields 사용
                        requestFields(
                                fieldWithPath("bookId").description("도서 ID"),
                                fieldWithPath("orderItemId").description("주문 아이템 ID"),
                                fieldWithPath("rating").description("별점 (1~5)"),
                                fieldWithPath("content").description("리뷰 내용")
                        )
                ));
    }

    @Test
    @DisplayName("도서별 리뷰 목록 조회")
    void getReviewsWithSummary() throws Exception {
        // Given
        Long bookId = 1L;
        Long userId = 1L;

        ReviewListResponse review = ReviewListResponse.builder()
                .reviewId(10L)
                .userId(userId)
                .userName("홍길동")
                .content("추천합니다.")
                .rating(5)
                .imageUrlList(List.of("http://test.com/image.jpg"))
                .createdAt(LocalDate.now())
                .isMyReview(true)
                .build();

        Page<ReviewListResponse> page = new PageImpl<>(List.of(review), PageRequest.of(0, 10), 1);

        given(reviewService.getReviewListByBookId(eq(bookId), any(), any(Pageable.class))).willReturn(page);
        given(bookReviewSummaryService.getSummary(bookId)).willReturn("4.5 (10 reviews)");

        // When & Then
        mockMvc.perform(get("/books/{book-id}/reviews", bookId)
                        .header("X-User-Id", String.valueOf(userId))
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewSummary").value("4.5 (10 reviews)"))
                .andExpect(jsonPath("$.data.reviews.content[0].reviewId").value(10L))
                .andExpect(jsonPath("$.data.reviews.content[0].myReview").value(true))
                .andDo(document("review-list-with-summary-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID (로그인 시)").optional()
                        ),
                        pathParameters(
                                parameterWithName("book-id").description("도서 ID")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.reviewSummary").description("리뷰 요약 정보"),
                                fieldWithPath("data.reviews.content[]").description("리뷰 목록"),
                                fieldWithPath("data.reviews.content[].reviewId").description("리뷰 ID"),
                                fieldWithPath("data.reviews.content[].userId").description("작성자 ID"),
                                fieldWithPath("data.reviews.content[].userName").description("작성자 이름"),
                                fieldWithPath("data.reviews.content[].content").description("리뷰 내용"),
                                fieldWithPath("data.reviews.content[].rating").description("평점"),
                                fieldWithPath("data.reviews.content[].imageUrlList").description("이미지 URL 목록"),
                                fieldWithPath("data.reviews.content[].createdAt").description("작성일"),
                                fieldWithPath("data.reviews.content[].myReview").description("본인 리뷰 여부"),
                                fieldWithPath("data.reviews.page").description("현재 페이지 번호"),
                                fieldWithPath("data.reviews.size").description("페이지 크기"),
                                fieldWithPath("data.reviews.totalElements").description("전체 리뷰 수"),
                                fieldWithPath("data.reviews.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.reviews.last").description("마지막 페이지 여부")
                        ))
                ));
    }
}