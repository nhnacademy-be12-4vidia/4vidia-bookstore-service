package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiRagService;
import com.nhnacademy._vidiabookstoreservice.book.domain.*;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.resolver.IsbnResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBookServiceImplTest {

    @Mock
    BookRepository bookRepository;
    @Mock
    GeminiRagService geminiRagService;
    @Mock
    TransactionTemplate transactionTemplate;

    @InjectMocks
    AdminBookServiceImpl adminBookService;

    @BeforeEach
    void setUp() {
        // lenient(): 어떤 테스트 중에 사용하지 않아도 에러 내지 말아라~
        // thenAnswer(): 이 함수 실행해서 나온 결과 반환해라~
        lenient().when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> {
                    TransactionCallback<?> callback = invocationOnMock.getArgument(0);
                    return callback.doInTransaction(new SimpleTransactionStatus());
                });
    }

    @Test
    @DisplayName("ISBN 검색 성공 - DB에 존재, Gemini 호출 안함")
    void processIsbnSearch_FoundInDb() {
        String inputIsbn = "333-22-4444";
        String normalizedIsbn = "333224444";

        Book mockBook = createComplexBook(normalizedIsbn);

        try (MockedStatic<IsbnResolver> isbnResolverMock = mockStatic(IsbnResolver.class)) { // static 메소드 조작은 closeable 필요
            isbnResolverMock.when(() -> IsbnResolver.toIsbn13(inputIsbn)).thenReturn(normalizedIsbn);

            given(bookRepository.findByIsbnWithDetails(normalizedIsbn)).willReturn(Optional.of(mockBook));

            AdminIsbnSearchResponse result = adminBookService.processIsbnSearch(inputIsbn);

            assertThat(result).isNotNull();
            assertThat(result.found()).isTrue();
            assertThat(result.isbn()).isEqualTo(normalizedIsbn);
            assertThat(result.title()).isEqualTo("테스트 책");

            assertThat(result.authors()).hasSize(1);
            assertThat(result.authors().get(0).name()).isEqualTo("김작가");

            verify(bookRepository).findByIsbnWithDetails(normalizedIsbn);
            verify(geminiRagService, never()).augmentBookInfo(normalizedIsbn, null);
        }
    }

    @Test
    @DisplayName("ISBN 검색 성공 - DB에 없음, Gemini 호출")
    void processIsbnSearch_NotFoundInDb() {
        String inputIsbn = "333-22-4444";
        String normalizedIsbn = "333224444";

        try (MockedStatic<IsbnResolver> isbnResolverMock = mockStatic(IsbnResolver.class)) {
            isbnResolverMock.when(() -> IsbnResolver.toIsbn13(inputIsbn)).thenReturn(normalizedIsbn);

            given(bookRepository.findByIsbnWithDetails(anyString())).willReturn(Optional.empty());

            AdminIsbnSearchResponse geminiResponse = mock(AdminIsbnSearchResponse.class);
            given(geminiRagService.augmentBookInfo(normalizedIsbn, null)).willReturn(geminiResponse);

            AdminIsbnSearchResponse result = adminBookService.processIsbnSearch(inputIsbn);

            assertThat(result).isEqualTo(geminiResponse);
            verify(bookRepository).findByIsbnWithDetails(normalizedIsbn);
            verify(geminiRagService).augmentBookInfo(normalizedIsbn, null);
        }
    }

    @Test
    @DisplayName("도서 정보 보강 성공 - DB에 책 있음")
    void augmentBookInfo_Success() {
        String inputIsbn = "333-22-4444";
        String normalizedIsbn = "333224444";

        Book mockBook = createComplexBook(normalizedIsbn);

        AdminIsbnSearchResponse expectResponse = mock(AdminIsbnSearchResponse.class);

        try (MockedStatic<IsbnResolver> isbnResolverMock = mockStatic(IsbnResolver.class)) {
            isbnResolverMock.when(() -> IsbnResolver.toIsbn13(inputIsbn)).thenReturn(normalizedIsbn);

            given(bookRepository.findByIsbnWithDetails(normalizedIsbn)).willReturn(Optional.of(mockBook));

            given(geminiRagService.augmentBookInfo(eq(normalizedIsbn), any())).willReturn(expectResponse);

            AdminIsbnSearchResponse result = adminBookService.augmentBookInfo(inputIsbn);

            assertThat(result).isEqualTo(expectResponse);

            verify(bookRepository).findByIsbnWithDetails(normalizedIsbn);
            verify(geminiRagService).augmentBookInfo(eq(normalizedIsbn), any());
        }
    }

    @Test
    @DisplayName("도서 정보 보강 성공 - DB에 책 없음")
    void augmentBookInfo_Fail_NotFoundInDb() {
        String inputIsbn = "333-22-9999";
        String normalizedIsbn = "333229999";

        try (MockedStatic<IsbnResolver> isbnResolverMock = mockStatic(IsbnResolver.class)) {
            isbnResolverMock.when(() -> IsbnResolver.toIsbn13(inputIsbn)).thenReturn(normalizedIsbn);

            given(bookRepository.findByIsbnWithDetails(normalizedIsbn)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminBookService.augmentBookInfo(inputIsbn))
                    .isInstanceOf(BookNotFoundException.class);

            verify(geminiRagService, never()).augmentBookInfo(anyString(), any());
        }
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