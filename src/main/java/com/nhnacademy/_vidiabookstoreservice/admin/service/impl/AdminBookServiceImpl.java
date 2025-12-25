package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiAnswerService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiRagService;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorNameRoleResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.redis.repository.AiSearchRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
            value = "adminIsbnSearch",
            key = "T(com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver).toIsbn13(#isbn)",
            cacheManager = "isbnSearchCacheManager",
            sync = true // 동시성 문제 방지: 동일 키에 대한 여러 요청이 동시에 들어올 때 하나의 요청만 처리하고 나머지는 대기하도록 설정
    )
    public AdminIsbnSearchResponse processIsbnSearch(String isbn) {
        String normalizedIsbn = IsbnResolver.toIsbn13(isbn);

        // 1. DB 조회: 있으면 바로 반환 (보강 X), 없으면 외부 검색 (보강 O)
        return bookRepository.findByIsbnWithDetails(normalizedIsbn)
                .map(AdminIsbnSearchResponse::foundFromDb)
                .orElseGet(() -> geminiRagService.augmentBookInfo(normalizedIsbn, null));
    }

    @Override
    @Cacheable(
            value = "bookAugmentation",
            key = "T(com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver).toIsbn13(#isbn)",
            cacheManager = "isbnSearchCacheManager",
            sync = true
    )
    public AdminIsbnSearchResponse augmentBookInfo(String isbn) {
        String normalizedIsbn = IsbnResolver.toIsbn13(isbn);

        // 1. DB 조회 (필수: 보강은 기존 도서가 있을 때만 가능)
        AdminIsbnSearchResponse dbBase = bookRepository.findByIsbnWithDetails(normalizedIsbn)
                .map(AdminIsbnSearchResponse::foundFromDb)
                .orElseThrow(() -> new BookNotFoundException(isbn));

        // 2. DB 데이터를 기반으로 외부 API + Gemini 보강 수행
        return geminiRagService.augmentBookInfo(normalizedIsbn, dbBase);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    value = "adminIsbnSearch",
                    key = "T(com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver).toIsbn13(#isbn)",
                    cacheManager = "isbnSearchCacheManager"),
            @CacheEvict(value = "bookAugmentation",
                    key = "T(com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver).toIsbn13(#isbn)",
                    cacheManager = "isbnSearchCacheManager")
    })
    public void evictIsbnCaches(String isbn) {
        log.info("Evicting ISBN caches for: {}", isbn);
    }
}