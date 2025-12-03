package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.exception.BookAuthorAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookAuthorRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookAuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookAuthorServiceImpl implements BookAuthorService {

    private final BookAuthorRepository bookAuthorRepository;


    @Override
    @Transactional
    public BookAuthor create(Book book, Author author, String role) {

        BookAuthor bookAuthor = BookAuthor.builder()
            .book(book)
            .author(author)
            .role(role)
            .build();

        return createByEntity(bookAuthor);
    }

    @Override
    @Transactional
    public BookAuthor createByEntity(BookAuthor bookAuthor) {

        if (bookAuthorRepository.existsByBookIdAndAuthorId(bookAuthor.getBook().getId(),
            bookAuthor.getAuthor().getId())) {
            throw new BookAuthorAlreadyExistsException(bookAuthor.getBook().getTitle(), bookAuthor.getAuthor().getName());
        }

        return bookAuthorRepository.save(bookAuthor);
    }
}
