package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.ImageAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookImageRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookImageServiceImpl implements BookImageService {

    private final BookImageRepository bookImageRepository;
    private final MinioService minioService;

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
        if (bookImageRepository.existsByBook_IdAndImageUrl(bookImage.getBook().getId(),
            bookImage.getImageUrl())) {
            throw new ImageAlreadyExistsException(bookImage.getBook().getTitle(), bookImage.getImageUrl());
        }

        return bookImageRepository.save(bookImage);

    }

    @Override
    @Transactional
    public void saveAll(List<BookImage> bookImages) {
        if (bookImages.isEmpty()) return;

        List<String> requestUrlList = bookImages.stream()
            .map(BookImage::getImageUrl).toList();

        Set<String> existingUrls = bookImageRepository.findExistingUrls(requestUrlList);

        List<BookImage> bookImageList = bookImages.stream()
            .filter(bi -> !existingUrls.contains(bi.getImageUrl()))
            .toList();

        if (!bookImageList.isEmpty()) {
            bookImageRepository.saveAll(bookImageList);
        }
    }

    @Override
    @Transactional
    public void replaceThumbnail(Book book, MultipartFile thumbnail) {

        BookImage existingImage = book.getBookImageList().stream()
            .filter(bi -> bi.getImageType().equals(ImageType.THUMBNAIL)).findFirst().orElse(null);

        if (Objects.nonNull(existingImage) && minioService.isMinioUrl(existingImage.getImageUrl())) {
            minioService.delete(existingImage.getImageUrl());
        }

        String newUrl = minioService.upload(thumbnail);

        if (Objects.nonNull(existingImage)) {
            existingImage.updateImageUrl(newUrl);
        } else {
            BookImage newBookImage = BookImage.builder()
                .book(book)
                .imageUrl(newUrl)
                .imageType(ImageType.THUMBNAIL)
                .displayOrder(0)
                .build();

            bookImageRepository.save(newBookImage);
            book.addBookImage(newBookImage);
        }

    }

    @Override
    @Transactional
    public void replaceThumbnail(Book book, String thumbnailUrl) {
        BookImage existingImage = book.getBookImageList().stream()
            .filter(bi -> bi.getImageType().equals(ImageType.THUMBNAIL))
            .findFirst()
            .orElse(null);

        if (Objects.nonNull(existingImage)) {
            if (minioService.isMinioUrl(existingImage.getImageUrl())) {
                minioService.delete(existingImage.getImageUrl());
            }
            existingImage.updateImageUrl(thumbnailUrl);
        } else {
            BookImage newBookImage = BookImage.builder()
                .book(book)
                .imageUrl(thumbnailUrl)
                .imageType(ImageType.THUMBNAIL)
                .displayOrder(0)
                .build();

            bookImageRepository.save(newBookImage);
            book.addBookImage(newBookImage);
        }
    }
}
