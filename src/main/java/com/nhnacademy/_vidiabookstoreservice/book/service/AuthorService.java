package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.dto.Author.response.AuthorIdResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.Author.response.AuthorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorService {

    Author getOrCreateAuthor(String name);

    AuthorIdResponse createAuthor(String name);

    AuthorIdResponse getAuthorByName(String name);

    Page<AuthorResponse> getAuthorList(String keyword, Pageable pageable);

    AuthorResponse getAuthorById(Long id);
}
