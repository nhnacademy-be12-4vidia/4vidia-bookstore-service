package com.nhnacademy._vidiabookstoreservice.book.service.event;

import com.nhnacademy._vidiabookstoreservice.book.client.OllamaFeignClient;
import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookSavedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookStockChangedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.event.ReviewRatingEvent;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.search.BookSearchRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventListener {

    private final BookSearchRepository bookSearchRepository;
    private final BookService bookService;
    private final OllamaFeignClient ollamaClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockChange(BookStockChangedEvent event) {
        log.info("ES 재고 업데이트 시작 - BookId: {}, NewStock: {}", event.bookId(), event.newStock());

        try {
            BookDocument document = bookSearchRepository.findById(String.valueOf(event.bookId()))
                .orElse(null);

            if (document != null) {
                BookDocument updatedDoc = document.toBuilder().stock(event.newStock()).build();

                bookSearchRepository.save(updatedDoc);
            }
        } catch (Exception e) {
            log.error("ES 재고 동기화 실패.");
        }
    }

    @Async
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookSaved(BookSavedEvent event) {

        Book savedBook = bookService.getBookEntity(event.bookId());

        String embeddingText = buildEmbeddingText(savedBook);

        var request = new OllamaFeignClient.EmbeddingRequest("bge-m3", embeddingText);
        var response = ollamaClient.generateEmbedding(request);

        double[] vector = response.embedding();

        BookDocument document = BookDocument.from(savedBook, vector);

        bookSearchRepository.save(document);
        log.info("ES 인덱싱 완료: Book ID {}", savedBook.getId());
    }

    private String buildEmbeddingText(Book book) {
        StringBuilder sb = new StringBuilder();
        sb.append("제목: ").append(book.getTitle()).append(" ");
        sb.append("작가: ").append(
            book.getBookAuthorList().stream()
                .map(ba -> ba.getAuthor().getName())
                .collect(Collectors.joining(", "))
        ).append(" ");
        sb.append("카테고리: ").append(book.getCategory().getCategoryName()).append(" ");
        sb.append("태그: ").append(
            book.getBookTagList().stream()
                .map(bt -> bt.getTag().getName())
                .collect(Collectors.joining(", "))
        ).append(" ");

        if (book.getDescription() != null) {
            String desc = book.getDescription().length() > 1000
                ? book.getDescription().substring(0, 1000)
                : book.getDescription();
            sb.append("설명: ").append(desc);
        }

        return sb.toString();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateAvgRating(ReviewRatingEvent event) {
        try {
            BookDocument bookDocument = bookSearchRepository.findById(
                String.valueOf(event.bookId())).orElseThrow(() -> new BookNotFoundException(
                event.bookId()));

            BookDocument updatedDocument = bookDocument.toBuilder().rating(event.rating()).build();

            bookSearchRepository.save(updatedDocument);
        } catch (Exception e) {
            log.error("es 리뷰 평점 변경 실패, bookId={}, rating={}",
                event.bookId(), event.rating(), e);
        }
    }

}
