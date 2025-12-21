package com.nhnacademy._vidiabookstoreservice.book.ai.gemini;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.aladin.client.AladinApiClient;
import com.nhnacademy._vidiabookstoreservice.book.aladin.dto.AladinItemDto;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiRagService {

    private final AladinApiClient aladinApiClient;
    private final RestClient restClient;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    public AdminIsbnSearchResponse augmentBookInfo(Book book) {
        return processAugmentation(book.getIsbn(), book);
    }

    public AdminIsbnSearchResponse augmentBookInfo(String isbn) {
        return processAugmentation(isbn, null);
    }

    private AdminIsbnSearchResponse processAugmentation(String isbn, Book existingBook) {

        // 1. 알라딘 API 호출
        AladinItemDto aladinItem = aladinApiClient.lookupByIsbn(
                restClient,
                isbn,
                aladinApiKey
        ).orElse(null);

        // 2. GEMINI RAG 호출

        // 3. 응답 조합 및 반환

        return null;
    }
}
