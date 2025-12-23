package com.nhnacademy._vidiabookstoreservice.book.dto.book.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
public abstract class BaseBookListResponse {
    protected Long id;
    protected String title;
    protected String isbn;
    protected Integer priceStandard;
    protected Integer priceSales;
    protected List<String> authorNames;
    protected String publisherName;
    protected String imageUrl;

    protected static <T extends BaseBookListResponseBuilder<?, ?>> T fillBase(T builder, Book book, String publisherDefaultMsg) {
        return (T) builder
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .priceStandard(book.getPriceStandard())
                .priceSales(book.getPriceSales())
                .authorNames(extractAuthorNames(book))
                .publisherName(extractPublisherName(book, publisherDefaultMsg))
                .imageUrl(extractThumbnailUrl(book));
    }

    protected static List<String> extractAuthorNames(Book book) {
        if (book.getBookAuthorList() == null) return List.of();

        return book.getBookAuthorList().stream()
                .map(BookAuthor::getAuthor)
                .map(Author::getName)
                .toList();
    }

    protected static String extractPublisherName(Book book, String defaultMsg) {
        if (book.getPublisher() == null) {
            return defaultMsg;
        }
        return book.getPublisher().getName();
    }

    protected static String extractThumbnailUrl(Book book) {
        if (book.getBookImageList() == null) return null;

        return book.getBookImageList().stream()
                .filter(img -> img.getImageType() == ImageType.THUMBNAIL)
                .findFirst()
                .map(BookImage::getImageUrl)
                .orElse(null);
    }
}
