package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.book.service.BookReviewSummaryService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest extends SupportControllerTest {

    @MockitoBean private ReviewService reviewService;
    @MockitoBean private BookReviewSummaryService bookReviewSummaryService;

    @BeforeEach
    void initData() { }

    @Test
    @DisplayName("[리뷰 생성]")
    void createReview() throws Exception {
        Long userId = 1L;
        Long bookId = 100L;

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookId(bookId);
        request.setOrderItemId(50L);
        request.setRating(5);
        request.setContent("리뷰 내용입니다.");

        doNothing().when(reviewService)
                .createReview(any(ReviewCreateRequest.class), anyLong(), any());

        mockMvc.perform(post("/books/{bookId}/reviews", bookId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
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
                        requestFields(
                                fieldWithPath("bookId").description("도서 ID"),
                                fieldWithPath("orderItemId").description("주문 아이템 ID"),
                                fieldWithPath("rating").description("별점 (1~5)"),
                                fieldWithPath("content").description("리뷰 내용")
                        )
                ));
    }


    @Test
    @DisplayName("[리뷰 요약 및 목록 조회]")
    void getReviewsWithSummary() throws Exception {
        Long userId = 1L;
        Long bookId = 100L;

        ReviewListResponse reviewDto = ReviewListResponse.builder()
                .reviewId(10L)
                .userId(55L)
                .userName("홍길동")
                .content("책이 정말 유익합니다.")
                .rating(5)
                .imageUrlList(List.of("https://image.url/review1.jpg"))
                .createdAt(LocalDate.of(2024, 12, 25))
                .myReview(true)
                .build();

        // Page 객체 생성
        Page<ReviewListResponse> reviewPage = new PageImpl<>(
                List.of(reviewDto),
                PageRequest.of(0, 10),
                1
        );

        String summaryMock = "이 책은 독자들에게 매우 긍정적인 평가를 받고 있습니다.";

        given(reviewService.getReviewListByBookId(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(reviewPage);
        given(bookReviewSummaryService.getSummary(anyLong()))
                .willReturn(summaryMock);

        mockMvc.perform(get("/books/{book-id}/reviews", bookId)
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(document("book-review-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("book-id").description("조회할 도서 ID")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        responseFields(
                                fieldWithPath("reviewSummary").type(JsonFieldType.STRING).description("AI 리뷰 요약 텍스트"),

                                // PageResponse<T> reviews
                                fieldWithPath("reviews").description("리뷰 페이징 정보 객체"),
                                fieldWithPath("reviews.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("reviews.size").type(JsonFieldType.NUMBER).description("페이지 당 데이터 수"),
                                fieldWithPath("reviews.totalElements").type(JsonFieldType.NUMBER).description("전체 데이터 수"),
                                fieldWithPath("reviews.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("reviews.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),

                                fieldWithPath("reviews.content[]").type(JsonFieldType.ARRAY).description("리뷰 데이터 목록"),
                                fieldWithPath("reviews.content[].reviewId").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("reviews.content[].userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("reviews.content[].userName").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("reviews.content[].content").type(JsonFieldType.STRING).description("리뷰 내용"),
                                fieldWithPath("reviews.content[].rating").type(JsonFieldType.NUMBER).description("별점 (1~5)"),
                                fieldWithPath("reviews.content[].imageUrlList").type(JsonFieldType.ARRAY).description("리뷰 이미지 URL 리스트"),
                                fieldWithPath("reviews.content[].createdAt").type(JsonFieldType.STRING).description("리뷰 작성일 (YYYY-MM-DD)"),

                                fieldWithPath("reviews.content[].myReview").type(JsonFieldType.BOOLEAN).description("본인 작성 리뷰 여부")
                        )
                ));
    }
}