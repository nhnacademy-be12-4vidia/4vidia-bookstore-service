package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.book.service.BookReviewSummaryService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

@WebMvcTest(ReviewController.class)
class ReviewControllerTest extends SupportControllerTest {

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private BookReviewSummaryService bookReviewSummaryService;

    @Test
    @Order(1)
    @DisplayName("GET - 리뷰 목록 조회 (요약포함)")
    void getReviewsWithSummary() throws Exception {
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
                .myReview(true)
                .build();

        Page<ReviewListResponse> page = new PageImpl<>(List.of(review), PageRequest.of(0, 10), 1);

        given(reviewService.getReviewListByBookId(eq(bookId), any(), any(Pageable.class))).willReturn(page);
        given(bookReviewSummaryService.getSummary(bookId)).willReturn("4.5 (10 reviews)");

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
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        pathParameters(
                                parameterWithName("book-id").description("도서 ID")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.reviewSummary").description("리뷰 요약 정보 (예: 평점 및 리뷰 수)"),
                                fieldWithPath("data.reviews.content[]").description("리뷰 목록"),
                                fieldWithPath("data.reviews.content[].reviewId").description("리뷰 ID"),
                                fieldWithPath("data.reviews.content[].userId").description("작성자 ID"),
                                fieldWithPath("data.reviews.content[].userName").description("작성자 이름"),
                                fieldWithPath("data.reviews.content[].content").description("리뷰 내용"),
                                fieldWithPath("data.reviews.content[].rating").description("평점 (1~5)"),
                                fieldWithPath("data.reviews.content[].imageUrlList").description("이미지 URL 목록"),
                                fieldWithPath("data.reviews.content[].createdAt").description("작성일"),
                                fieldWithPath("data.reviews.content[].myReview").description("본인 리뷰 여부"),
                                fieldWithPath("data.reviews.content[].modified").description("수정 여부"),

                                fieldWithPath("data.reviews.page").description("현재 페이지 번호 (0부터 시작)"),
                                fieldWithPath("data.reviews.size").description("페이지당 항목 수"),
                                fieldWithPath("data.reviews.totalElements").description("전체 데이터 개수"),
                                fieldWithPath("data.reviews.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.reviews.last").description("마지막 페이지 여부")
                        ))
                ));
    }

    @Test
    @Order(2)
    @DisplayName("POST - 리뷰 생성")
    void createReview() throws Exception {
        Long bookId = 1L;
        Long userId = 1L;

        mockMvc.perform(post("/books/{bookId}/reviews", bookId)
                        .header("X-User-Id", userId)
                        .param("bookId", String.valueOf(bookId))
                        .param("orderItemId", "100")
                        .param("rating", "5")
                        .param("content", "정말 좋은 책입니다!")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(document("book-review-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        pathParameters(
                                parameterWithName("bookId").description("도서 ID (URL 경로)")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(3)
    @DisplayName("POST - 리뷰 수정")
    void editReview() throws Exception {
        Long bookId = 1L;
        Long reviewId = 10L;
        Long userId = 1L;

        mockMvc.perform(post("/books/{book-id}/reviews/{review-id}/edit", bookId, reviewId)
                        .header("X-User-Id", userId)
                        .header("Referer", "http://localhost:8080/previous-page")
                        .param("reviewId", String.valueOf(reviewId))
                        .param("bookId", String.valueOf(bookId))
                        .param("content", "수정된 리뷰 내용입니다.")
                        .param("rating", "4")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(document("review-edit-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("book-id").description("도서 ID"),
                                parameterWithName("review-id").description("리뷰 ID")
                        ),
                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID"),
                                headerWithName("Referer").description("이전 페이지 주소").optional()
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(4)
    @DisplayName("POST - 리뷰 삭제")
    void deactivateReview() throws Exception {
        mockMvc.perform(post("/books/{book-id}/reviews/{review-id}/deactivate", 1L, 10L)
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andDo(document("review-deactivate-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("book-id").description("도서 ID"),
                                parameterWithName("review-id").description("리뷰 ID")
                        ),
                        responseFields(withHeader())
                ));
    }

}