package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiRagService;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBookServiceImpl implements AdminBookService {

    private final BookRepository bookRepository;
    private final GeminiRagService geminiRagService;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Cacheable(
            value = "adminIsbnSearch",
            key = "T(com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver).toIsbn13(#isbn)",
            cacheManager = "isbnSearchCacheManager",
            sync = true // 동시성 문제 방지: 동일 키에 대한 여러 요청이 동시에 들어올 때 하나의 요청만 처리하고 나머지는 대기하도록 설정
    )
    public AdminIsbnSearchResponse processIsbnSearch(String isbn) {
        String normalizedIsbn = IsbnResolver.toIsbn13(isbn);

        // 1. DB 조회 및 DTO 변환 (TransactionTemplate 사용으로 트랜잭션 범위 최소화)
        // 지연 로딩(Lazy Loading)이 발생하는 foundFromDb() 호출 시점까지 트랜잭션을 유지하고, 이후 즉시 커넥션 반환
        AdminIsbnSearchResponse dbResponse = transactionTemplate.execute(status -> 
            bookRepository.findByIsbnWithDetails(normalizedIsbn)
                .map(AdminIsbnSearchResponse::foundFromDb)
                .orElse(null)
        );

        if (dbResponse != null) {
            return dbResponse;
        }

        // 2. 외부 검색 (트랜잭션 없이 실행 -> Long Transaction 방지)
        return geminiRagService.augmentBookInfo(normalizedIsbn, null);
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
        AdminIsbnSearchResponse dbBase = transactionTemplate.execute(status ->
            bookRepository.findByIsbnWithDetails(normalizedIsbn)
                .map(AdminIsbnSearchResponse::foundFromDb)
                .orElseThrow(() -> new BookNotFoundException(isbn))
        );

        // 2. DB 데이터를 기반으로 외부 API + Gemini 보강 수행 (트랜잭션 없이 실행)
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
        log.info("[관리자 도서] CacheEvicting ISBN caches for: {}", isbn);
    }
}