package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookTag;
import com.nhnacademy._vidiabookstoreservice.book.domain.Tag;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.BookTagAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookTagRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookTagServiceImpl implements BookTagService {

    private final BookTagRepository bookTagRepository;

    @Override
    @Transactional
    public BookTag create(Book book, Tag tag) {

        BookTag bookTag = BookTag.builder()
            .book(book)
            .tag(tag)
            .build();

        return createByEntity(bookTag);
    }

    @Override
    @Transactional
    public BookTag createByEntity(BookTag bookTag) {

        if (bookTagRepository.existsByBook_IdAndTag_Id(bookTag.getBook().getId(),
            bookTag.getTag().getId())) {
            throw new BookTagAlreadyExistsException(bookTag.getBook().getTitle(), bookTag.getTag().getName());
        }
        return bookTagRepository.save(bookTag);
    }
}
