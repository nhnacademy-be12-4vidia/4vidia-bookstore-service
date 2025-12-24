package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiAnswerService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiRagService;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorNameRoleResponse;
import com.nhnacademy._vidiabookstoreservice.book.redis.repository.AiSearchRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBookServiceImpl implements AdminBookService {

    private final BookRepository bookRepository;
    private final GeminiRagService geminiRagService;

    @Override
    @Cacheable(
            value = "adminIsbnSearchV8",
            key = "T(com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver).toIsbn13(#isbn)",
            cacheManager = "isbnSearchCacheManager",
            sync = true // 동시에 여러 요청이 들어올 때 하나의 요청만 처리하고 나머지는 대기
    )
    public AdminIsbnSearchResponse processIsbnSearch(String isbn) {
        String normalizedIsbn = IsbnResolver.toIsbn13(isbn);

        // 1. DB 조회
        return bookRepository.findByIsbn(normalizedIsbn)
                .map(book -> {
                    // 2. 보강 필요 여부 판단
                    boolean needsAugmentation =
                            !StringUtils.hasText(book.getDescription())
                            || !StringUtils.hasText(book.getBookIndex());
                    AdminIsbnSearchResponse response =
                            AdminIsbnSearchResponse.foundFromDb(book);
                    if (!needsAugmentation) {
                        // 2-1. 보강 필요X: DB에서 정보 조회 후 반환
                        return response;
                    } else {
                        // 2-2. 보강 필요O: 알라딘 조회 + GEMINI RAG 후 반환
                        return geminiRagService.augmentBookInfo(normalizedIsbn, response);
                    }
                }).orElseGet(() -> {
                    // 3. DB에 없는 경우: 알라딘 조회 + GEMINI RAG 후 반환
                    return geminiRagService.augmentBookInfo(normalizedIsbn, null);
                });
    }
}