package com.nhnacademy._vidiabookstoreservice.book.aladin.client;

import com.nhnacademy._vidiabookstoreservice.book.aladin.dto.AladinItemDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(AladinApiClient.class)
class AladinApiClientTest {

    @Autowired
    private AladinApiClient aladinApiClient;

    private MockRestServiceServer mockRestServiceServer;
    private RestClient testRestClient;

    private final String apiKey = "test-api-key";
    private final String isbn = "9788912345678";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.mockRestServiceServer = MockRestServiceServer.bindTo(builder).build();
        this.testRestClient = builder.build();
    }

    @AfterEach
    void tearDown() {
        mockRestServiceServer.verify();
    }

    @Test
    @DisplayName("알라딘 정상 조회: 알라딘 API가 유효한 아이템 반환")
    void lookUpByIsbn_Success() {
        String successJson = """
                {
                    "version": "20131101",
                    "totalResults": 1,
                    "startIndex": 1,
                    "itemsPerPage": 1,
                    "item": [
                        {
                            "title": "클린 코드",
                            "author": "로버트 C. 마틴",
                            "pubDate": "2013-12-24",
                            "description": "애자일 소프트웨어 장인 정신...",
                            "isbn": "8966260956",
                            "isbn13": "9788912345678",
                            "priceStandard": 33000,
                            "cover": "http://image.aladin.co.kr/cover.jpg",
                            "categoryName": "국내도서>IT/모바일>프로그래밍",
                            "publisher": "인사이트"
                        }
                    ]
                }
                """;

        mockRestServiceServer.expect(requestTo(containsString(isbn)))
                .andRespond(withSuccess(successJson, MediaType.APPLICATION_JSON));

        Optional<AladinItemDto> result = aladinApiClient.lookupByIsbn(testRestClient, isbn, apiKey);

        assertThat(result).isPresent();
        AladinItemDto itemDto = result.get();

        assertThat(itemDto.isbn13()).isEqualTo(isbn);
        assertThat(itemDto.title()).isEqualTo("클린 코드");
        assertThat(itemDto.author()).isEqualTo("로버트 C. 마틴");
        assertThat(itemDto.priceStandard()).isEqualTo(33000);
        assertThat(itemDto.publisher()).isEqualTo("인사이트");
    }

    @Test
    @DisplayName("알라딘 조회 실패: 에러 코드가 포함된 에러 응답")
    void lookUpByIsbn_ApiError() {
        String errorJson = """
                {
                    "errorCode": 200,
                    "errorMessage": "Invalid ItemId"
                }
                """;

        mockRestServiceServer.expect(requestTo(containsString(isbn)))
                .andRespond(withSuccess(errorJson, MediaType.APPLICATION_JSON));

        Optional<AladinItemDto> result = aladinApiClient.lookupByIsbn(testRestClient, isbn, apiKey);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("알라딘 조회 실패: 검색 결과 없음 (item 리스트 비어있음)")
    void lookupByIsbn_EmptyItems() {
        String emptyJson = """
                {
                    "version": "20131101",
                    "item": []
                }
                """;

        mockRestServiceServer.expect(requestTo(containsString(isbn)))
                .andRespond(withSuccess(emptyJson, MediaType.APPLICATION_JSON));

        Optional<AladinItemDto> result = aladinApiClient.lookupByIsbn(testRestClient, isbn, apiKey);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("정상 조회: URL 파라미터가 모두 정확하게 포함되어 있는지 검증")
    void lookupByIsbn_UrlCheck() {
        String successJson = """
                {
                    "version": "20131101",
                    "item": []
                }
                """;

        mockRestServiceServer.expect(requestTo(allOf(
                        containsString("http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx"), // 기본 URL
                        containsString("ttbkey=" + apiKey),
                        containsString("ItemId=" + isbn),
                        containsString("ItemIdType=ISBN13"),
                        containsString("Cover=Big"),
                        containsString("Output=JS"),
                        containsString("Version=20131101")
                )))
                .andRespond(withSuccess(successJson, MediaType.APPLICATION_JSON));

        aladinApiClient.lookupByIsbn(testRestClient, isbn, apiKey);

        mockRestServiceServer.verify();
    }
}