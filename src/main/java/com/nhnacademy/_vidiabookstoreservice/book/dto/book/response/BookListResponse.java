package com.nhnacademy._vidiabookstoreservice.book.dto.book.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookListResponse {

    private Long id;
    private String title;
    private String isbn;
    private Integer priceStandard;
    private Integer priceSales;
    private List<String> authorNames;
    private String publisherName;
    private String imageUrl;

    private boolean liked;

    public static BookListResponse from(Book book) {
        return BookListResponse.builder()
            .id(book.getId())
            .title(book.getTitle())
            .isbn(book.getIsbn())
            .priceStandard(book.getPriceStandard())
            .priceSales(book.getPriceSales())
            .authorNames(book.getBookAuthorList().stream().map(BookAuthor::getAuthor).map(
                Author::getName).toList())
            .publisherName(book.getPublisher().getName())
            .imageUrl(book.getBookImageList().stream().filter(i -> i.getImageType().equals(
                ImageType.THUMBNAIL)).findFirst().map(BookImage::getImageUrl).orElse(null))
            .build();
    }

    // 회원용
    public static BookListResponse from(Book book, boolean isLiked) {
        return BookListResponse.builder()
            .id(book.getId())
            .title(book.getTitle())
            .isbn(book.getIsbn())
            .priceStandard(book.getPriceStandard())
            .priceSales(book.getPriceSales())
            .authorNames(book.getBookAuthorList().stream().map(BookAuthor::getAuthor).map(
                Author::getName).toList())
            .publisherName(book.getPublisher() != null ? book.getPublisher().getName() : "출판사 정보가 없습니다")
            .imageUrl(book.getBookImageList().stream().filter(i -> i.getImageType().equals(
                ImageType.THUMBNAIL)).findFirst().map(BookImage::getImageUrl).orElse(null))
            .liked(isLiked) // 넘겨줄거
            .build();
    }
}
