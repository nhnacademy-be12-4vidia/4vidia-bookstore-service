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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(BookController.class)
class BookControllerTest extends SupportControllerTest {

    @MockitoBean private BookService bookService;
    @MockitoBean private BookSearchService bookSearchService;
    @MockitoBean private ReviewService reviewService;
    @MockitoBean private StringRedisTemplate bestsellerRedisTemplate;
    @MockitoBean private BookReviewSummaryService bookReviewSummaryService;

    private final Long testUserId = 1L;

    @Test
    @DisplayName("GET - Elastic 검색")
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
                .andDo(document("book-elastic-search-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        queryParameters(
                                parameterWithName("keyword").description("검색어"),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional(),
                                parameterWithName("sort").description("정렬 조건").optional()
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.page.totalElements").description("전체 검색 결과 수"),
                                fieldWithPath("data.page.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.page.page").description("현재 페이지 번호"),
                                fieldWithPath("data.page.size").description("페이지 크기"),
                                fieldWithPath("data.page.last").description("마지막 페이지 여부"),
                                fieldWithPath("data.page.content").description("도서 목록"),

                                fieldWithPath("data.page.content[].id").description("도서 ID"),
                                fieldWithPath("data.page.content[].title").description("제목"),
                                fieldWithPath("data.page.content[].isbn").description("ISBN"),
                                fieldWithPath("data.page.content[].priceStandard").description("정가"),
                                fieldWithPath("data.page.content[].priceSales").description("판매가"),
                                fieldWithPath("data.page.content[].authorNames").description("작가 목록"),
                                fieldWithPath("data.page.content[].publisherName").description("출판사"),
                                fieldWithPath("data.page.content[].imageUrl").description("이미지 URL"),
                                fieldWithPath("data.page.content[].liked").description("좋아요 여부").optional(),

                                fieldWithPath("data.aiCacheResponseList").description("AI 추천 캐시 목록 (있으면)")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - AI 검색")
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
                .andExpect(jsonPath("$.data.aiAnswer").value("AI 답변입니다."))
                .andDo(document("book-search-ai-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        queryParameters(
                                parameterWithName("keyword").description("질문 내용")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.aiAnswer").description("AI 답변"),
                                fieldWithPath("data.results").description("검색 결과 페이징 정보"),
                                fieldWithPath("data.results.content").description("도서 목록"),
                                fieldWithPath("data.results.page").description("페이지 번호"),
                                fieldWithPath("data.results.size").description("페이지 크기"),
                                fieldWithPath("data.results.totalElements").description("전체 수"),
                                fieldWithPath("data.results.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.results.last").description("마지막 페이지 여부")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 태그 검색(다중)")
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
                        responseFields(withHeader(
                                fieldWithPath("data.content").description("도서 목록"),
                                fieldWithPath("data.page").description("페이지 번호"),
                                fieldWithPath("data.size").description("페이지 크기"),
                                fieldWithPath("data.totalElements").description("전체 수"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.last").description("마지막 페이지 여부")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 태그 검색(단일)")
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
                        responseFields(withHeader(
                                fieldWithPath("data.content").description("도서 목록"),
                                fieldWithPath("data.page").description("페이지 번호"),
                                fieldWithPath("data.size").description("페이지 크기"),
                                fieldWithPath("data.totalElements").description("전체 수"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.last").description("마지막 페이지 여부")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 도서 상세 조회")
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
                .andExpect(jsonPath("$.data.id").value(bookId))
                .andDo(document("book-detail-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("bookId").description("도서 ID")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.id").description("도서 ID"),
                                fieldWithPath("data.isbn").description("ISBN"),
                                fieldWithPath("data.title").description("제목"),
                                fieldWithPath("data.subtitle").description("부제").optional(),
                                fieldWithPath("data.bookIndex").description("목차").optional(),
                                fieldWithPath("data.description").description("설명"),
                                fieldWithPath("data.publishedDate").description("출판일"),
                                fieldWithPath("data.priceStandard").description("정가"),
                                fieldWithPath("data.priceSales").description("판매가"),
                                fieldWithPath("data.stock").description("재고"),
                                fieldWithPath("data.stockStatus").description("재고 상태"),
                                fieldWithPath("data.packagingAvailable").description("포장 가능 여부"),
                                fieldWithPath("data.pageCount").description("페이지 수").optional(),
                                fieldWithPath("data.language").description("언어").optional(),
                                fieldWithPath("data.volumeNumber").description("권 호수").optional(),
                                fieldWithPath("data.reviewCount").description("리뷰 수"),
                                fieldWithPath("data.avgRating").description("평균 평점"),
                                fieldWithPath("data.publisher").description("출판사 정보").optional(),
                                fieldWithPath("data.publisher.id").description("출판사 ID").optional(),
                                fieldWithPath("data.publisher.name").description("출판사 명").optional(),
                                fieldWithPath("data.category").description("카테고리 정보").optional(),
                                fieldWithPath("data.category.id").description("카테고리 ID").optional(),
                                fieldWithPath("data.category.name").description("카테고리 명").optional(),
                                fieldWithPath("data.category.kdcCode").description("KDC 코드").optional(),
                                fieldWithPath("data.authors").description("저자 목록"),
                                fieldWithPath("data.authors[].id").description("저자 ID"),
                                fieldWithPath("data.authors[].name").description("저자 명"),
                                fieldWithPath("data.authors[].role").description("저자 역할"),
                                fieldWithPath("data.tags").description("태그 목록"),
                                fieldWithPath("data.imageUrls").description("이미지 목록").optional()
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 베스트셀러 조회")
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
                .andExpect(jsonPath("$.data.size()").value(2))
                .andDo(document("book-bestseller-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        responseFields(withHeader(
                                fieldWithPath("data[].id").description("도서 ID"),
                                fieldWithPath("data[].title").description("제목"),
                                fieldWithPath("data[].isbn").description("ISBN").optional(),
                                fieldWithPath("data[].priceStandard").description("정가"),
                                fieldWithPath("data[].priceSales").description("판매가"),
                                fieldWithPath("data[].authorNames").description("저자 목록").optional(),
                                fieldWithPath("data[].publisherName").description("출판사").optional(),
                                fieldWithPath("data[].imageUrl").description("이미지 URL").optional(),
                                fieldWithPath("data[].liked").description("좋아요 여부")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 도서 검색(simple)")
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
                        responseFields(withHeader(
                                fieldWithPath("data[].id").description("도서 ID"),
                                fieldWithPath("data[].title").description("제목"),
                                fieldWithPath("data[].isbn").description("ISBN").optional(),
                                fieldWithPath("data[].priceStandard").description("정가").optional(),
                                fieldWithPath("data[].priceSales").description("판매가").optional(),
                                fieldWithPath("data[].authorNames").description("저자 목록").optional(),
                                fieldWithPath("data[].publisherName").description("출판사").optional(),
                                fieldWithPath("data[].imageUrl").description("이미지 URL").optional(),
                                fieldWithPath("data[].liked").description("좋아요 여부").optional()
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 도서 목록 조회")
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
                        responseFields(withHeader(
                                fieldWithPath("data[].id").description("도서 ID"),
                                fieldWithPath("data[].title").description("제목"),
                                fieldWithPath("data[].isbn").description("ISBN").optional(),
                                fieldWithPath("data[].priceStandard").description("정가").optional(),
                                fieldWithPath("data[].priceSales").description("판매가").optional(),
                                fieldWithPath("data[].authorNames").description("저자 목록").optional(),
                                fieldWithPath("data[].publisherName").description("출판사").optional(),
                                fieldWithPath("data[].imageUrl").description("이미지 URL").optional(),
                                fieldWithPath("data[].liked").description("좋아요 여부")
                        ))
                ));
    }
}