package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BaseBookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookDetailResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.AiBookSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.AiCacheResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.SearchBooksResponse;
import com.nhnacademy._vidiabookstoreservice.book.service.BookReviewSummaryService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import com.nhnacademy._vidiabookstoreservice.book.service.search.BookSearchService;
import com.nhnacademy._vidiabookstoreservice.book.utils.BookSortKey;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerTest extends SupportControllerTest {

    @MockitoBean private BookService bookService;
    @MockitoBean private BookSearchService bookSearchService;
    @MockitoBean private ReviewService reviewService;
    @MockitoBean private StringRedisTemplate bestsellerRedisTemplate;
    @MockitoBean private BookReviewSummaryService bookReviewSummaryService;

    private final Long testUserId = 1L;

    @Test
    @DisplayName("[통합 검색 (ES)]")
    void searchBooks() throws Exception {
        Page<BaseBookListResponse> page = new PageImpl<>(List.of(
                BookListResponse.builder()
                        .id(1L)
                        .title("테스트 책")
                        .isbn("12345")
                        .priceStandard(10000)
                        .priceSales(9000)
                        .authorNames(List.of("작가1"))
                        .publisherName("출판사")
                        .imageUrl("url")
                        .liked(false)
                        .build()
        ));
        PageResponse<BaseBookListResponse> pageResponse = PageResponse.from(page);

        List<AiCacheResponse> aiCache = List.of();

        SearchBooksResponse mockResponse = new SearchBooksResponse(pageResponse, aiCache);

        given(bookSearchService.searchBooks(any(), any(Pageable.class), any()))
                .willReturn(mockResponse);

        mockMvc.perform(get("/books/search")
                        .header("X-User-Id", testUserId)
                        .param("keyword", "테스트")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("book-search-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(headerWithName("X-User-Id").description("회원 ID").optional()),
                        queryParameters(
                                parameterWithName("keyword").description("검색어"),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional(),
                                parameterWithName("sort").description("정렬 조건").optional()
                        ),
                        responseFields(
                                fieldWithPath("page.totalElements").description("전체 검색 결과 수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 수"),
                                fieldWithPath("page.page").description("현재 페이지 번호"),
                                fieldWithPath("page.size").description("페이지 크기"),
                                fieldWithPath("page.last").description("마지막 페이지 여부"),
                                fieldWithPath("page.content").description("도서 목록"),

                                fieldWithPath("page.content[].id").description("도서 ID"),
                                fieldWithPath("page.content[].title").description("제목"),
                                fieldWithPath("page.content[].isbn").description("ISBN"),
                                fieldWithPath("page.content[].priceStandard").description("정가"),
                                fieldWithPath("page.content[].priceSales").description("판매가"),
                                fieldWithPath("page.content[].authorNames").description("작가 목록"),
                                fieldWithPath("page.content[].publisherName").description("출판사"),
                                fieldWithPath("page.content[].imageUrl").description("이미지 URL"),
                                fieldWithPath("page.content[].liked").description("좋아요 여부").optional(),

                                fieldWithPath("aiCacheResponseList").description("AI 추천 캐시 목록 (있으면)")
                        )
                ));
    }

    @Test
    @DisplayName("[AI 검색]")
    void searchBooksWithLlm() throws Exception {
        Page<BaseBookListResponse> page = new PageImpl<>(List.of());
        PageResponse<BaseBookListResponse> pageResponse = PageResponse.from(page);

        AiBookSearchResponse mockResponse = AiBookSearchResponse.builder()
                .results(pageResponse)
                .aiAnswer("AI 답변입니다.")
                .build();

        given(bookSearchService.searchBookWithLlm(any(), any(Pageable.class), any()))
                .willReturn(mockResponse);

        mockMvc.perform(get("/books/search/ai")
                        .header("X-User-Id", testUserId)
                        .param("keyword", "추천해줘")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiAnswer").value("AI 답변입니다."))
                .andDo(document("book-search-ai-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("keyword").description("질문 내용")
                        ),
                        responseFields(
                                fieldWithPath("aiAnswer").description("AI 답변"),
                                fieldWithPath("results").description("검색 결과 페이징 정보"),
                                fieldWithPath("results.content").description("도서 목록"),
                                fieldWithPath("results.page").description("페이지 번호"),
                                fieldWithPath("results.size").description("페이지 크기"),
                                fieldWithPath("results.totalElements").description("전체 수"),
                                fieldWithPath("results.totalPages").description("전체 페이지 수"),
                                fieldWithPath("results.last").description("마지막 페이지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[태그 기반 검색 (다중)]")
    void searchBooksWithTags() throws Exception {
        Page<BaseBookListResponse> page = new PageImpl<>(List.of());
        PageResponse<BaseBookListResponse> pageResponse = PageResponse.from(page);

        given(bookSearchService.searchBooksByTags(any(), any(Pageable.class), any()))
                .willReturn(pageResponse);

        mockMvc.perform(get("/books/search/tags")
                        .param("tagIds", "1,2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("book-search-tags-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("tagIds").description("태그 ID 목록 (콤마 구분)")
                        ),
                        responseFields(
                                fieldWithPath("content").description("도서 목록"),
                                fieldWithPath("page").description("페이지 번호"),
                                fieldWithPath("size").description("페이지 크기"),
                                fieldWithPath("totalElements").description("전체 수"),
                                fieldWithPath("totalPages").description("전체 페이지 수"),
                                fieldWithPath("last").description("마지막 페이지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[특정 태그 기반 검색]")
    void searchBooksWithSpecificTag() throws Exception {
        Page<BaseBookListResponse> page = new PageImpl<>(List.of());
        PageResponse<BaseBookListResponse> pageResponse = PageResponse.from(page);

        given(bookService.getBooksByTag(eq(1L), any(), any(BookSortKey.class), anyBoolean(), any(Pageable.class), any()))
                .willReturn(pageResponse);

        mockMvc.perform(get("/books/search/tags/{tag-id}", 1L)
                        .param("tagName", "소설")
                        .param("sortKey", "PRICE")
                        .param("direction", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("book-search-specific-tag-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("tag-id").description("태그 ID")
                        ),
                        queryParameters(
                                parameterWithName("tagName").description("태그 이름").optional(),
                                parameterWithName("sortKey").description("정렬 키").optional(),
                                parameterWithName("direction").description("정렬 방향 (asc/desc)").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        responseFields(
                                fieldWithPath("content").description("도서 목록"),
                                fieldWithPath("page").description("페이지 번호"),
                                fieldWithPath("size").description("페이지 크기"),
                                fieldWithPath("totalElements").description("전체 수"),
                                fieldWithPath("totalPages").description("전체 페이지 수"),
                                fieldWithPath("last").description("마지막 페이지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[도서 상세 조회]")
    void bookDetails() throws Exception {
        Long bookId = 1L;
        BookDetailResponse mockResponse = BookDetailResponse.builder()
                .id(bookId)
                .isbn("9791112345678")
                .title("책 제목")
                .description("설명")
                .publishedDate(LocalDate.now())
                .priceStandard(10000)
                .priceSales(9000)
                .stock(100)
                .stockStatus("IN_STOCK")
                .packagingAvailable(true)
                .publisher(BookDetailResponse.PublisherInfo.builder().id(1L).name("출판사").build())
                .category(BookDetailResponse.CategoryInfo.builder().id(1L).name("카테고리").build())
                .authors(List.of(BookDetailResponse.AuthorInfo.builder().id(1L).name("저자").role("AUTHOR").build()))
                .imageUrls(List.of("url1"))
                .tags(List.of("tag1"))
                .reviewCount(10L)
                .avgRating(4.5)
                .build();

        given(bookService.getBookDetail(bookId)).willReturn(mockResponse);

        mockMvc.perform(get("/books/{bookId}", bookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andDo(document("book-detail-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("bookId").description("도서 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").description("도서 ID"),
                                fieldWithPath("isbn").description("ISBN"),
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("subtitle").description("부제").optional(),
                                fieldWithPath("bookIndex").description("목차").optional(),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("publishedDate").description("출판일"),
                                fieldWithPath("priceStandard").description("정가"),
                                fieldWithPath("priceSales").description("판매가"),
                                fieldWithPath("stock").description("재고"),
                                fieldWithPath("stockStatus").description("재고 상태"),
                                fieldWithPath("packagingAvailable").description("포장 가능 여부"),
                                fieldWithPath("pageCount").description("페이지 수").optional(),
                                fieldWithPath("language").description("언어").optional(),
                                fieldWithPath("volumeNumber").description("권 호수").optional(),
                                fieldWithPath("reviewCount").description("리뷰 수"),
                                fieldWithPath("avgRating").description("평균 평점"),
                                fieldWithPath("publisher").description("출판사 정보").optional(),
                                fieldWithPath("publisher.id").description("출판사 ID").optional(),
                                fieldWithPath("publisher.name").description("출판사 명").optional(),
                                fieldWithPath("category").description("카테고리 정보").optional(),
                                fieldWithPath("category.id").description("카테고리 ID").optional(),
                                fieldWithPath("category.name").description("카테고리 명").optional(),
                                fieldWithPath("category.kdcCode").description("KDC 코드").optional(),
                                fieldWithPath("authors").description("저자 목록"),
                                fieldWithPath("authors[].id").description("저자 ID"),
                                fieldWithPath("authors[].name").description("저자 명"),
                                fieldWithPath("authors[].role").description("저자 역할"),
                                fieldWithPath("tags").description("태그 목록"),
                                fieldWithPath("imageUrls").description("이미지 목록").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[베스트셀러 조회]")
    void getBestSellers() throws Exception {
        ListOperations listOperations = mock(ListOperations.class);
        given(bestsellerRedisTemplate.opsForList()).willReturn(listOperations);

        given(listOperations.range("view:bestseller:top10", 0, -1))
                .willReturn(List.of("1", "2"));

        List<BookListResponse> responses = List.of(
                BookListResponse.builder().id(1L).title("title1").priceStandard(1000).priceSales(900).build(),
                BookListResponse.builder().id(2L).title("title2").priceStandard(2000).priceSales(1800).build()
        );
        given(bookService.getBookListResponseByIdList(anyList(), any())).willReturn(responses);

        mockMvc.perform(get("/books/best-seller")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andDo(document("book-bestseller-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("[].id").description("도서 ID"),
                                fieldWithPath("[].title").description("제목"),
                                fieldWithPath("[].isbn").description("ISBN").optional(),
                                fieldWithPath("[].priceStandard").description("정가"),
                                fieldWithPath("[].priceSales").description("판매가"),
                                fieldWithPath("[].authorNames").description("저자 목록").optional(),
                                fieldWithPath("[].publisherName").description("출판사").optional(),
                                fieldWithPath("[].imageUrl").description("이미지 URL").optional(),
                                fieldWithPath("[].liked").description("좋아요 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[쿠폰용 간단 도서 검색]")
    void getBookList() throws Exception {
        List<BookListResponse> responses = List.of(
                BookListResponse.builder().id(1L).title("검색된 책").build()
        );

        given(bookSearchService.searchBooksForCoupon("검색")).willReturn(responses);

        mockMvc.perform(get("/books/search-simple")
                        .param("keyword", "검색")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("book-search-simple-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("keyword").description("검색어")
                        ),
                        responseFields(
                                fieldWithPath("[].id").description("도서 ID"),
                                fieldWithPath("[].title").description("제목"),
                                fieldWithPath("[].isbn").description("ISBN").optional(),
                                fieldWithPath("[].priceStandard").description("정가").optional(),
                                fieldWithPath("[].priceSales").description("판매가").optional(),
                                fieldWithPath("[].authorNames").description("저자 목록").optional(),
                                fieldWithPath("[].publisherName").description("출판사").optional(),
                                fieldWithPath("[].imageUrl").description("이미지 URL").optional(),
                                fieldWithPath("[].liked").description("좋아요 여부").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[메인 페이지 도서 목록 조회 (태그별)]")
    void getMainBookList() throws Exception {
        List<BookListResponse> responses = List.of(
                BookListResponse.builder().id(1L).title("메인 책").liked(true).build()
        );

        given(bookService.getMainBookList(eq(10L), any())).willReturn(responses);

        mockMvc.perform(get("/books/main-list")
                        .param("tagId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("book-main-list-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("tagId").description("조회할 태그 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].id").description("도서 ID"),
                                fieldWithPath("[].title").description("제목"),
                                fieldWithPath("[].isbn").description("ISBN").optional(),
                                fieldWithPath("[].priceStandard").description("정가").optional(),
                                fieldWithPath("[].priceSales").description("판매가").optional(),
                                fieldWithPath("[].authorNames").description("저자 목록").optional(),
                                fieldWithPath("[].publisherName").description("출판사").optional(),
                                fieldWithPath("[].imageUrl").description("이미지 URL").optional(),
                                fieldWithPath("[].liked").description("좋아요 여부")
                        )
                ));
    }
}