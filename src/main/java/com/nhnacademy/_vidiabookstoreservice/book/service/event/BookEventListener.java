package com.nhnacademy._vidiabookstoreservice.book.service.event;

import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookSavedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookStockChangedEvent;
import com.nhnacademy._vidiabookstoreservice.book.repository.search.BookSearchRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventListener {

    private final BookSearchRepository bookSearchRepository;
    private final BookService bookService;

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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookSaved(BookSavedEvent event) {

        Book savedBook = bookService.getBookEntity(event.bookId());

        BookDocument document = BookDocument.from(savedBook);

        bookSearchRepository.save(document);
    }

}
