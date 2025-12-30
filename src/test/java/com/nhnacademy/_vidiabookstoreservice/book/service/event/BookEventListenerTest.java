package com.nhnacademy._vidiabookstoreservice.book.service.event;

import com.nhnacademy._vidiabookstoreservice.book.client.OllamaFeignClient;
import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.domain.*;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookSavedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookStockChangedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.event.ReviewRatingEvent;
import com.nhnacademy._vidiabookstoreservice.book.repository.search.BookSearchRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookEventListenerTest {

    @InjectMocks
    BookEventListener bookEventListener;

    @Mock
    BookSearchRepository bookSearchRepository;
    @Mock
    BookService bookService;
    @Mock
    OllamaFeignClient ollamaFeignClient;

    @Test
    @DisplayName("재고 변경 이벤트 처리: ES 문서 존재하면 재고 업데이트")
    void handleStockChange_Success() {
        Long bookId = 1L;
        Integer newStock = 50;
        BookStockChangedEvent event = new BookStockChangedEvent(bookId, newStock);

        BookDocument existingDoc = BookDocument.builder()
                .id(String.valueOf(bookId))
                .stock(10)
                .title("기존 책")
                .build();

        given(bookSearchRepository.findById(String.valueOf(bookId))).willReturn(Optional.of(existingDoc));

        bookEventListener.handleStockChange(event);

        ArgumentCaptor<BookDocument> captor = ArgumentCaptor.forClass(BookDocument.class);
        verify(bookSearchRepository).save(captor.capture());

        BookDocument savedDoc = captor.getValue();
        assertThat(savedDoc.getId()).isEqualTo(String.valueOf(bookId));
        assertThat(savedDoc.getStock()).isEqualTo(newStock);
        assertThat(savedDoc.getTitle()).isEqualTo("기존 책");
    }

    @Test
    @DisplayName("도서 저장 이벤트 처리 - Book 엔티티를 Document로 변환하여 저장")
    void handleBookSaved_Success() {
        Long bookId = 1L;
        BookSavedEvent event = new BookSavedEvent(bookId, "테스트 책");

        Book mockBook = createComplexBook(bookId);

        given(bookService.getBookEntity(bookId)).willReturn(mockBook);

        double[] mockVector = {0.1, 0.2, 0.3};
        OllamaFeignClient.EmbeddingResponse mockResponse = new OllamaFeignClient.EmbeddingResponse(mockVector);
        given(ollamaFeignClient.generateEmbedding(any())).willReturn(mockResponse);

        bookEventListener.handleBookSaved(event);

        ArgumentCaptor<OllamaFeignClient.EmbeddingRequest> requestCaptor =
                ArgumentCaptor.forClass(OllamaFeignClient.EmbeddingRequest.class);
        verify(ollamaFeignClient).generateEmbedding(requestCaptor.capture());

        String prompt = requestCaptor.getValue().prompt();
        assertThat(prompt).contains("제목: 테스트 책");
        assertThat(prompt).contains("작가: 김작가");

        ArgumentCaptor<BookDocument> docCaptor = ArgumentCaptor.forClass(BookDocument.class);
        verify(bookSearchRepository).save(docCaptor.capture());

        BookDocument savedDoc = docCaptor.getValue();

        // BookDocument 필드 매핑 검증
        assertThat(savedDoc.getId()).isEqualTo("1");
        assertThat(savedDoc.getTitle()).isEqualTo("테스트 책");
        assertThat(savedDoc.getPublisher()).isEqualTo("NHN출판");
        assertThat(savedDoc.getAuthors()).containsExactly("김작가");
        assertThat(savedDoc.getTags()).containsExactly("베스트셀러");
        assertThat(savedDoc.getEmbedding()).isEqualTo(mockVector);
    }

    @Test
    @DisplayName("평점 변경 이벤트 처리 - toBuilder 이용한 부분 업데이트 확인")
    void updateAvgRating_Success() {
        Long bookId = 1L;
        Double newRating = 4.8;
        ReviewRatingEvent event = new ReviewRatingEvent(bookId, newRating);

        BookDocument existingDoc = BookDocument.builder()
                .id(String.valueOf(bookId))
                .rating(3.5)
                .title("테스트 책")
                .build();

        given(bookSearchRepository.findById(String.valueOf(bookId)))
                .willReturn(Optional.of(existingDoc));

        bookEventListener.updateAvgRating(event);

        ArgumentCaptor<BookDocument> captor = ArgumentCaptor.forClass(BookDocument.class);
        verify(bookSearchRepository).save(captor.capture());

        BookDocument savedDoc = captor.getValue();
        assertThat(savedDoc.getRating()).isEqualTo(newRating);
        assertThat(savedDoc.getTitle()).isEqualTo("테스트 책");
    }

    // -- helper Methods --
    private <T> T createEntity(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Entity 생성 실패: " + clazz.getSimpleName(), e);
        }
    }

    private Book createComplexBook(Long id) {
        // 1. Publisher (BookDocument.from에서 getName 호출함)
        Publisher publisher = createEntity(Publisher.class);
        ReflectionTestUtils.setField(publisher, "name", "NHN출판");

        // 2. Author & BookAuthor (List 매핑용)
        Author author = createEntity(Author.class);
        ReflectionTestUtils.setField(author, "name", "김작가");

        BookAuthor bookAuthor = createEntity(BookAuthor.class);
        ReflectionTestUtils.setField(bookAuthor, "author", author);

        // 3. Tag & BookTag (List 매핑용)
        Tag tag = createEntity(Tag.class);
        ReflectionTestUtils.setField(tag, "name", "베스트셀러");

        BookTag bookTag = createEntity(BookTag.class);
        ReflectionTestUtils.setField(bookTag, "tag", tag);

        // 4. Category (임베딩 텍스트 생성용)
        Category category = createEntity(Category.class);
        ReflectionTestUtils.setField(category, "categoryName", "IT/컴퓨터");

        // 5. Book 조립
        Book book = createEntity(Book.class);
        ReflectionTestUtils.setField(book, "id", id);
        ReflectionTestUtils.setField(book, "title", "테스트 책");
        ReflectionTestUtils.setField(book, "isbn", "978891234");
        ReflectionTestUtils.setField(book, "description", "설명입니다.");
        ReflectionTestUtils.setField(book, "priceSales", 10000);
        ReflectionTestUtils.setField(book, "stock", 50);

        // 연관관계 주입
        ReflectionTestUtils.setField(book, "publisher", publisher);
        ReflectionTestUtils.setField(book, "category", category);
        ReflectionTestUtils.setField(book, "bookAuthorList", List.of(bookAuthor));
        ReflectionTestUtils.setField(book, "bookTagList", List.of(bookTag));

        return book;
    }

}