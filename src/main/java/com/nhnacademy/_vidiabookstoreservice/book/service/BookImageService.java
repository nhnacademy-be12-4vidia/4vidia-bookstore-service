package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface BookImageService {

    BookImage create(Book book, String imageUrl, ImageType imageType, Integer displayOrder);

    BookImage createByEntity(BookImage bookImage);

    void saveAll(List<BookImage> bookImages);

    void replaceThumbnail(Book book, MultipartFile thumbnail);

    void replaceThumbnail(Book book, String thumbnailUrl);

}
