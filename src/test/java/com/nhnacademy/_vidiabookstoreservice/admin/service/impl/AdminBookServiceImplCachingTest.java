package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiRagService;
import com.nhnacademy._vidiabookstoreservice.book.domain.*;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {
        AdminBookServiceImpl.class,
        AdminBookServiceImplCachingTest.TestCacheConfig.class
})
class AdminBookServiceImplCachingTest {

    @MockitoBean
    private BookRepository bookRepository;
    @MockitoBean
    private GeminiRagService geminiRagService;
    @MockitoBean
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AdminBookService adminBookService;

    @TestConfiguration
    @EnableCaching
    static class TestCacheConfig { // 테스트는 Redis 대신 Map 기반 캐시 매니저 사용
        @Bean
        public CacheManager isbnSearchCacheManager() {
            return new ConcurrentMapCacheManager("adminIsbnSearch", "bookAugmentation");
        }
    }

    @BeforeEach
    void setUp() {
        given(transactionTemplate.execute(any())).willAnswer(invocationOnMock -> {
            TransactionCallback<?> callback = invocationOnMock.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
    }

    @Test
    @DisplayName("캐싱 동작 확인 - 동일 ISBN으로 두 번 조회 시, DB 조회는 한 번만 발생")
    void processIsbnSearch_CacheHit() {
        String inputIsbn = "979-11-56759270";
        String normalizedIsbn = "9791156759270";

        Book mockBook = createComplexBook(normalizedIsbn);
        given(bookRepository.findByIsbnWithDetails(normalizedIsbn)).willReturn(Optional.of(mockBook));

        adminBookService.processIsbnSearch(inputIsbn);

        adminBookService.processIsbnSearch(inputIsbn);

        verify(bookRepository, times(1)).findByIsbnWithDetails(normalizedIsbn);
    }

    @Test
    @DisplayName("캐싱 삭제 확인 - 삭제 후 조회 시 다시 DB 접근")
    void evictIsbnCaches_Test() {
        String inputIsbn = "979-11-56759270";
        String normalizedIsbn = "9791156759270";

        Book mockBook = createComplexBook(normalizedIsbn);
        given(bookRepository.findByIsbnWithDetails(normalizedIsbn)).willReturn(Optional.of(mockBook));

        adminBookService.processIsbnSearch(inputIsbn);

        adminBookService.evictIsbnCaches(inputIsbn);

        adminBookService.processIsbnSearch(inputIsbn);

        verify(bookRepository, times(2)).findByIsbnWithDetails(normalizedIsbn);
    }


    // -- helper Methods --
    private <T> T createEntity(Class<T> clazz, Object... fieldNameAndValues) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T entity = constructor.newInstance();
            for (int i = 0; i < fieldNameAndValues.length; i += 2) {
                ReflectionTestUtils.setField(entity, (String) fieldNameAndValues[i], fieldNameAndValues[i + 1]);
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("엔티티 생성 실패: " + clazz.getSimpleName(), e);
        }
    }

    private Book createComplexBook(String isbn) {
        Publisher publisher = createEntity(Publisher.class, "name", "NHN출판");

        Author author = createEntity(Author.class, "name", "김작가");
        BookAuthor bookAuthor = createEntity(BookAuthor.class, "author", author, "role", "지은이");

        Tag tag = createEntity(Tag.class, "name", "베스트셀러");
        BookTag bookTag = createEntity(BookTag.class, "tag", tag);

        BookImage bookImage = createEntity(BookImage.class,
                "imageUrl", "http://image.com/1.jpg",
                "imageType", ImageType.THUMBNAIL
        );

        return createEntity(Book.class,
                "id", 1L,
                "isbn", isbn,
                "title", "테스트 책",
                "publisher", publisher,
                "bookAuthorList", List.of(bookAuthor),
                "bookTagList", List.of(bookTag),
                "bookImageList", List.of(bookImage)
        );
    }

}

