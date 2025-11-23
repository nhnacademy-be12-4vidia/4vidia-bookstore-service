package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;
import com.nhnacademy._vidiabookstoreservice.book.dto.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.response.BookIdResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.BookAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.AuthorRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookAuthorRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.PublisherRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookAuthorRepository bookAuthorRepository;
    private final PublisherRepository publisherRepository;

    @Override
    @Transactional
    public BookIdResponse createBook(BookCreateRequest request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookAlreadyExistsException(
                "이미 존재하는 도서입니다. ISBN : %s".formatted(request.getIsbn()));
        }

        Publisher publisher = getOrSavePublisher(request.getPublisherName());
        Book savedBook = bookRepository.save(request.toEntity(publisher));

        saveAuthors(savedBook, request.getAuthorList(), "지은이");
        saveAuthors(savedBook, request.getContributorList(), "기여자/역자");

        BookIdResponse bookIdDto = new BookIdResponse();
        bookIdDto.setId(savedBook.getId());
        return bookIdDto;
    }

    private Publisher getOrSavePublisher(String publisherName) {
        if (publisherName == null || publisherName.isBlank()) {
            return null;
        }
        return publisherRepository.findByName(publisherName).orElseGet(() ->
            publisherRepository.save(new Publisher(publisherName.trim())));
    }


    private void saveAuthors(Book book, String nameStr, String role) {
        if (nameStr == null || nameStr.isBlank()) {
            return;
        }

        String[] names = nameStr.split(",");

        for (String name : names) {
            String cleanName = name.trim();
            if (cleanName.isEmpty()) continue;
            Author author = authorRepository.findByName(cleanName)
                .orElseGet(() -> authorRepository.save(new Author(cleanName)));

            BookAuthor bookAuthor = BookAuthor.builder()
                .author(author)
                .book(book)
                .role(role)
                .build();

            bookAuthorRepository.save(bookAuthor);
        }
    }
}
