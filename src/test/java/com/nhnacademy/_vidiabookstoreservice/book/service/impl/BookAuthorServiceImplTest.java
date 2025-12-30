package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.BookAuthorAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookAuthorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookAuthorServiceImplTest {

    @Mock
    private BookAuthorRepository bookAuthorRepository;

    @InjectMocks
    private BookAuthorServiceImpl bookAuthorService;

    @Test
    @DisplayName("도서-저자 관계 생성 - 성공")
    void create_success() {
        Book book = mock(Book.class);
        Author author = mock(Author.class);
        String role = "Author";

        when(book.getId()).thenReturn(1L);
        when(author.getId()).thenReturn(1L);
        when(bookAuthorRepository.existsByBookIdAndAuthorId(1L, 1L)).thenReturn(false);
        when(bookAuthorRepository.save(any(BookAuthor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookAuthor result = bookAuthorService.create(book, author, role);

        assertNotNull(result);
        assertEquals(book, result.getBook());
        assertEquals(author, result.getAuthor());
        assertEquals(role, result.getRole());
        verify(bookAuthorRepository, times(1)).save(any(BookAuthor.class));
    }

    @Test
    @DisplayName("도서-저자 관계 생성 - 이미 존재할 경우 예외 발생")
    void create_alreadyExists_throwsException() {
        Book book = mock(Book.class);
        Author author = mock(Author.class);

        when(book.getId()).thenReturn(1L);
        when(author.getId()).thenReturn(1L);
        when(book.getTitle()).thenReturn("Test Book");
        when(author.getName()).thenReturn("Test Author");
        when(bookAuthorRepository.existsByBookIdAndAuthorId(1L, 1L)).thenReturn(true);

        assertThrows(BookAuthorAlreadyExistsException.class, () -> {
            bookAuthorService.create(book, author, "Author");
        });
        verify(bookAuthorRepository, never()).save(any(BookAuthor.class));
    }
}