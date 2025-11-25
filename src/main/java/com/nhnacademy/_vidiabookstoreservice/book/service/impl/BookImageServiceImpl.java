package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.exception.BookImageAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookImageRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookImageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookImageServiceImpl implements BookImageService {

    private final BookImageRepository bookImageRepository;

    @Override
    @Transactional
    public BookImage create(Book book, String imageUrl, ImageType imageType, Integer displayOrder) {

        BookImage bookImage = BookImage.builder()
            .book(book)
            .imageUrl(imageUrl)
            .imageType(imageType)
            .displayOrder(displayOrder)
            .build();

        return createByEntity(bookImage);
    }

    @Override
    @Transactional
    public BookImage createByEntity(BookImage bookImage) {
        if (bookImageRepository.existsByBookIdAndImageUrl(bookImage.getBook().getId(),
            bookImage.getImageUrl())) {
            throw new BookImageAlreadyExistsException(
                "해당 Url은 이미 저장되어있습니다. 도서: %s, Url: %s".formatted(bookImage.getBook().getTitle(),
                    bookImage.getImageUrl()));
        }

        return bookImageRepository.save(bookImage);

    }

    @Override
    @Transactional
    public void saveAll(List<BookImage> bookImages) {
        if (bookImages.isEmpty()) return;
        bookImageRepository.saveAll(bookImages);
    }
}
