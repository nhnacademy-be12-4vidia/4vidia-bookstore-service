package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorIdResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.AuthorAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.AuthorIdNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.exception.AuthorNameNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.AuthorRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional
    public Author getOrCreateAuthor(String name) {
        String cleanName = name.trim();
        return authorRepository.findByName(name).orElseGet(() -> authorRepository.save(new Author(cleanName)));
    }

    @Override
    public AuthorIdResponse createAuthor(String name) {

        String cleanName = name.trim();

        if (authorRepository.existsByName(cleanName)) {
            throw new AuthorAlreadyExistsException(
                "해당하는 이름의 작가가 이미 존재합니다. 이름: %s".formatted(cleanName));
        }
        Author author = authorRepository.save(new Author(cleanName));
        return new AuthorIdResponse(author.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorIdResponse getAuthorByName(String name) {
        Author author = authorRepository.findByName(name).orElseThrow(
            () -> new AuthorNameNotFoundException(
                "해당하는 이름의 작가를 찾을 수 없습니다. 이름: %s".formatted(name)));
        return new AuthorIdResponse(author.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuthorResponse> getAuthorList(String keyword, Pageable pageable) {

        Page<Author> authors;

        if (StringUtils.hasText(keyword)) {
            authors = authorRepository.findByNameContaining(keyword, pageable);
        } else {
            authors = authorRepository.findAll(pageable);
        }
        return authors.map(AuthorResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorResponse getAuthorById(Long id) {
        Author author = authorRepository.findById(id).orElseThrow(
            () -> new AuthorIdNotFoundException("아이디에 해당하는 작가를 찾을 수 없습니다. 작가ID: %d".formatted(id)));

        return new AuthorResponse(author.getId(), author.getName());
    }
}
