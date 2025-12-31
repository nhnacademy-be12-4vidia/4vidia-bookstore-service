package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookTag;
import com.nhnacademy._vidiabookstoreservice.book.domain.Tag;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.BookTagAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookTagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookTagServiceImplTest {

    @Mock
    private BookTagRepository bookTagRepository;

    @InjectMocks
    private BookTagServiceImpl bookTagService;

    @Test
    @DisplayName("도서-태그 관계 생성 - 성공")
    void create_success() {
        Book book = mock(Book.class);
        Tag tag = mock(Tag.class);

        when(book.getId()).thenReturn(1L);
        when(tag.getId()).thenReturn(10L);
        when(bookTagRepository.existsByBook_IdAndTag_Id(1L, 10L)).thenReturn(false);
        when(bookTagRepository.save(any(BookTag.class))).thenAnswer(inv -> inv.getArgument(0));

        BookTag result = bookTagService.create(book, tag);

        assertNotNull(result);
        assertEquals(book, result.getBook());
        assertEquals(tag, result.getTag());
        verify(bookTagRepository, times(1)).save(any(BookTag.class));
    }

    @Test
    @DisplayName("도서-태그 관계 생성 - 이미 존재할 경우 예외 발생")
    void create_alreadyExists_throwsException() {
        Book book = mock(Book.class);
        Tag tag = mock(Tag.class);

        when(book.getId()).thenReturn(1L);
        when(tag.getId()).thenReturn(10L);
        when(book.getTitle()).thenReturn("Test Book");
        when(tag.getName()).thenReturn("Test Tag");
        when(bookTagRepository.existsByBook_IdAndTag_Id(1L, 10L)).thenReturn(true);

        assertThrows(BookTagAlreadyExistsException.class, () -> bookTagService.create(book, tag));
        verify(bookTagRepository, never()).save(any(BookTag.class));
    }
}