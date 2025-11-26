package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;

public interface BookAuthorService {

    BookAuthor create(Book book, Author author, String role);

    BookAuthor createByEntity(BookAuthor bookAuthor);

}
