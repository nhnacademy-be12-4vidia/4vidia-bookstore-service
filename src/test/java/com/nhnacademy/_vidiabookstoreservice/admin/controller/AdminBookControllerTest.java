package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.BookIsbnResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorNameRoleResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.mq.producer.StorageEventProducer;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.impl.MinioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBookController.class)
class AdminBookControllerTest extends SupportControllerTest {

    @MockitoBean private AdminBookService adminBookService;
    @MockitoBean private BookService bookService;
    @MockitoBean private MinioService minioService;
    @MockitoBean private StorageEventProducer storageEventProducer;

    @Test
    @DisplayName("GET - ISBN으로 도서 정보 검색")
    void searchBookByIsbn() throws Exception {
        String isbn = "9788960777330";
        AdminIsbnSearchResponse response = new AdminIsbnSearchResponse(
                true, 1L, isbn, "http://image.url", "테스트 도서", "부제목",
                List.of(new AuthorNameRoleResponse("작가", "지음")), "출판사",
                LocalDate.of(2024, 1, 1), "한국어", 300, "KDC123",
                20000, 100, StockStatus.IN_STOCK, true,
                "설명", "목차", List.of("태그1")
        );

        given(adminBookService.processIsbnSearch(isbn)).willReturn(response);

        mockMvc.perform(get("/admin/books/search")
                        .param("isbn", isbn)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isbn").value(isbn))
                .andDo(document("admin-book-search-isbn-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(parameterWithName("isbn").description("검색할 ISBN")),
                        responseFields(withHeader(
                                fieldWithPath("data.found").description("도서 존재 여부"),
                                fieldWithPath("data.bookId").description("도서 ID").optional(),
                                fieldWithPath("data.isbn").description("ISBN"),
                                fieldWithPath("data.coverImageUrl").description("표지 이미지 URL"),
                                fieldWithPath("data.title").description("제목"),
                                fieldWithPath("data.subtitle").description("부제목"),
                                fieldWithPath("data.authors[].name").description("작가 이름"),
                                fieldWithPath("data.authors[].role").description("역할"),
                                fieldWithPath("data.publisher").description("출판사"),
                                fieldWithPath("data.publishedDate").description("출판일"),
                                fieldWithPath("data.language").description("언어"),
                                fieldWithPath("data.pageCount").description("페이지 수"),
                                fieldWithPath("data.categoryCode").description("카테고리 코드"),
                                fieldWithPath("data.priceStandard").description("정가"),
                                fieldWithPath("data.stock").description("재고"),
                                fieldWithPath("data.stockStatus").description("재고 상태"),
                                fieldWithPath("data.packagingAvailable").description("포장 가능 여부"),
                                fieldWithPath("data.description").description("설명"),
                                fieldWithPath("data.bookIndex").description("목차"),
                                fieldWithPath("data.tags[]").description("태그 목록")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - ISBN 정보 보강")
    void augmentBookInfo() throws Exception {
        String isbn = "9788960777330";
        AdminIsbnSearchResponse response = new AdminIsbnSearchResponse(
                true, 1L, isbn, "http://image.url", "정보 보강 도서", "부제목",
                List.of(new AuthorNameRoleResponse("작가", "지음")), "출판사",
                LocalDate.of(2024, 1, 1), "한국어", 300, "KDC123",
                20000, 100, StockStatus.IN_STOCK, true,
                "설명", "목차", List.of("태그1")
        );

        given(adminBookService.augmentBookInfo(isbn)).willReturn(response);

        mockMvc.perform(get("/admin/books/augment")
                        .param("isbn", isbn)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isbn").value(isbn))
                .andDo(document("admin-book-augment-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(parameterWithName("isbn").description("정보를 보강할 ISBN")),
                        responseFields(withHeader(
                                fieldWithPath("data.found").description("도서 존재 여부"),
                                fieldWithPath("data.bookId").description("도서 ID").optional(),
                                fieldWithPath("data.isbn").description("ISBN"),
                                fieldWithPath("data.coverImageUrl").description("표지 이미지 URL"),
                                fieldWithPath("data.title").description("제목"),
                                fieldWithPath("data.subtitle").description("부제목"),
                                fieldWithPath("data.authors[].name").description("작가 이름"),
                                fieldWithPath("data.authors[].role").description("역할"),
                                fieldWithPath("data.publisher").description("출판사"),
                                fieldWithPath("data.publishedDate").description("출판일"),
                                fieldWithPath("data.language").description("언어"),
                                fieldWithPath("data.pageCount").description("페이지 수"),
                                fieldWithPath("data.categoryCode").description("카테고리 코드"),
                                fieldWithPath("data.priceStandard").description("정가"),
                                fieldWithPath("data.stock").description("재고"),
                                fieldWithPath("data.stockStatus").description("재고 상태"),
                                fieldWithPath("data.packagingAvailable").description("포장 가능 여부"),
                                fieldWithPath("data.description").description("설명"),
                                fieldWithPath("data.bookIndex").description("목차"),
                                fieldWithPath("data.tags[]").description("태그 목록")
                        ))
                ));
    }

    @Test
    @DisplayName("POST - 새 도서 등록")
    void createBook() throws Exception {
        BookCreateRequest request = new BookCreateRequest();
        ReflectionTestUtils.setField(request, "isbn", "9788960777330");
        ReflectionTestUtils.setField(request, "title", "새 도서");
        ReflectionTestUtils.setField(request, "publisherName", "테스트출판");
        ReflectionTestUtils.setField(request, "categoryId", 1L);
        ReflectionTestUtils.setField(request, "stockStatus", StockStatus.IN_STOCK);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        given(bookService.createBook(any(), any())).willReturn(null);

        mockMvc.perform(multipart("/admin/books")
                        .file(requestPart))
                .andExpect(status().isOk())
                .andDo(document("admin-book-create-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(partWithName("request").description("도서 생성 정보 (JSON)")),
                        responseFields(withHeader())
                ));
    }

    @Test
    @DisplayName("PUT - 도서 정보 수정")
    void updateBook() throws Exception {
        Long bookId = 1L;
        BookUpdateRequest request = new BookUpdateRequest();
        ReflectionTestUtils.setField(request, "title", "수정된 제목");
        ReflectionTestUtils.setField(request, "publisherName", "수정된 출판사");
        ReflectionTestUtils.setField(request, "categoryId", 1L);
        ReflectionTestUtils.setField(request, "stockStatus", StockStatus.OUT_OF_STOCK);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        doNothing().when(bookService).updateBook(eq(bookId), any(), any());

        mockMvc.perform(multipart("/admin/books/{book-id}", bookId)
                        .file(requestPart)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andDo(document("admin-book-update-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("book-id").description("수정할 도서 ID")),
                        requestParts(partWithName("request").description("도서 수정 정보 (JSON)")),
                        responseFields(withHeader())
                ));
    }

    @Test
    @DisplayName("GET - 도서 ISBN 조회")
    void getBookIsbn() throws Exception {
        Long bookId = 1L;
        String isbn = "9788960777330";
        given(bookService.getBookIsbnById(bookId)).willReturn(isbn);

        mockMvc.perform(get("/admin/books/{book-id}/isbn", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isbn").value(isbn))
                .andDo(document("admin-book-isbn-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("book-id").description("도서 ID")),
                        responseFields(withHeader(
                                fieldWithPath("data.isbn").description("해당 도서의 ISBN")
                        ))
                ));
    }

    @Test
    @DisplayName("POST - 상세 이미지 업로드")
    void uploadImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.png", "image/png", "data".getBytes());
        String expectedUrl = "http://minio.url/test.png";

        given(minioService.upload(any())).willReturn(expectedUrl);

        mockMvc.perform(multipart("/admin/books/images")
                        .file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value(expectedUrl))
                .andDo(document("admin-book-image-upload-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(partWithName("image").description("업로드할 이미지 파일")),
                        responseFields(withHeader(
                                fieldWithPath("data.url").description("업로드된 이미지의 공개 URL")
                        ))
                ));
    }
}