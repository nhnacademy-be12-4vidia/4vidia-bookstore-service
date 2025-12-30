package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorIdResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.AuthorAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.AuthorNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.AuthorRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;


    @Test
    @DisplayName("getOrCreateAuthor - 이미 존재하는 저자면 조회 결과 반환")
    void getOrCreateAuthor_exists_returnsFound() {
        String name = "김작가";
        Author author = new Author(name);
        when(authorRepository.findByName(name)).thenReturn(Optional.of(author));

        Author result = authorService.getOrCreateAuthor(name);

        assertEquals(name, result.getName());
        verify(authorRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrCreateAuthor - 존재하지 않으면 새로 생성 후 반환")
    void getOrCreateAuthor_notExists_createsNew() {
        String originalName = "  이작가  ";
        String cleanName = "이작가";

        when(authorRepository.findByName(originalName)).thenReturn(Optional.empty());

        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> {
            Author author = inv.getArgument(0);
            return author;
        });

        Author result = authorService.getOrCreateAuthor(originalName);

        assertEquals(cleanName, result.getName());
        verify(authorRepository).findByName(originalName);
        verify(authorRepository).save(argThat(author -> author.getName().equals(cleanName)));
    }


    @Test
    @DisplayName("createAuthor - 성공")
    void createAuthor_success() {
        String name = "박작가";
        Author author = mock(Author.class);
        when(author.getId()).thenReturn(1L);
        when(authorRepository.existsByName(name)).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        AuthorIdResponse result = authorService.createAuthor(name);

        assertEquals(1L, result.getId());
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    @DisplayName("createAuthor - 중복된 이름일 경우 예외 발생")
    void createAuthor_alreadyExists_throwsException() {
        String name = "이미있는저자";
        when(authorRepository.existsByName(name)).thenReturn(true);

        assertThrows(AuthorAlreadyExistsException.class, () -> authorService.createAuthor(name));
    }

    @Test
    @DisplayName("getAuthorByName - 성공")
    void getAuthorByName_success() {
        String name = "최작가";
        Author author = mock(Author.class);
        when(author.getId()).thenReturn(10L);
        when(authorRepository.findByName(name)).thenReturn(Optional.of(author));

        AuthorIdResponse result = authorService.getAuthorByName(name);

        assertEquals(10L, result.getId());
    }

    @Test
    @DisplayName("getAuthorByName - 저자 없을 때 예외 발생")
    void getAuthorByName_notFound_throwsException() {
        when(authorRepository.findByName(anyString())).thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class, () -> authorService.getAuthorByName("없는저자"));
    }

    @Test
    @DisplayName("getAuthorList - 키워드가 있을 때 (Containing 조회)")
    void getAuthorList_withKeyword_success() {
        String keyword = "김";
        Pageable pageable = PageRequest.of(0, 10);
        Author author = new Author("김작가");
        Page<Author> page = new PageImpl<>(List.of(author));

        when(authorRepository.findByNameContaining(keyword, pageable)).thenReturn(page);

        Page<AuthorResponse> result = authorService.getAuthorList(keyword, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("김작가", result.getContent().get(0).getName());
        verify(authorRepository).findByNameContaining(keyword, pageable);
    }

    @Test
    @DisplayName("getAuthorList - 키워드가 없을 때 (전체 조회)")
    void getAuthorList_noKeyword_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Author> page = new PageImpl<>(List.of(new Author("작가1"), new Author("작가2")));

        when(authorRepository.findAll(pageable)).thenReturn(page);

        Page<AuthorResponse> result = authorService.getAuthorList("", pageable); // 빈 문자열

        assertEquals(2, result.getTotalElements());
        verify(authorRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getAuthorById - 성공")
    void getAuthorById_success() {
        Long id = 1L;
        Author author = mock(Author.class);
        when(author.getId()).thenReturn(id);
        when(author.getName()).thenReturn("정작가");
        when(authorRepository.findById(id)).thenReturn(Optional.of(author));

        AuthorResponse result = authorService.getAuthorById(id);

        assertEquals(id, result.getId());
        assertEquals("정작가", result.getName());
    }

    @Test
    @DisplayName("getAuthorById - 존재하지 않는 ID일 때 예외 발생 (ID 기반 NotFound)")
    void getAuthorById_notFound_throwsException() {
        when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class, () -> authorService.getAuthorById(999L));
    }
}