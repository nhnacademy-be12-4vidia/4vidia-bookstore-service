package com.nhnacademy._vidiabookstoreservice.book.aladin.client;

import com.nhnacademy._vidiabookstoreservice.book.aladin.dto.AladinItemDto;
import com.nhnacademy._vidiabookstoreservice.book.aladin.dto.AladinResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AladinApiClient {

    private static final String LOOK_UP_URL = "http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx";
    private static final String ITEM_ID_TYPE = "ISBN13";
    private static final String COVER_SIZE = "Big";
    private static final String OUTPUT_FORMAT = "JS";
    private static final String VERSION = "20131101";

    @Retryable(
            retryFor = {RestClientException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public Optional<AladinItemDto> lookupByIsbn(
            RestClient restClient,
            String isbn13,
            String apiKey
    ) {
        String url = buildLookUpUrl(isbn13, apiKey);

        AladinResponseDto response = restClient
                .get()
                .uri(url)
                .retrieve()
                .body(AladinResponseDto.class);

        if (response == null) {
            return Optional.empty();
        }

        // 에러 응답 체크 (200 OK로 에러가 오는 경우)
        if (response.hasError()) {
            return Optional.empty();
        }

        if (response.item() == null || response.item().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(response.item().getFirst());
    }

    private String buildLookUpUrl(String isbn13, String apiKey) {
        return UriComponentsBuilder.fromUriString(LOOK_UP_URL)
                .queryParam("ttbkey", apiKey)
                .queryParam("ItemIdType", ITEM_ID_TYPE)
                .queryParam("ItemId", isbn13)
                .queryParam("Cover", COVER_SIZE)
                .queryParam("Output", OUTPUT_FORMAT)
                .queryParam("Version", VERSION)
                .build()
                .toUriString();
    }
}
