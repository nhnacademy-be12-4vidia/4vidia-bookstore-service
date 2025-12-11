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
@Builder(toBuilder = true)
public class BookSearchListResponse {

    private Long id;
    private String title;
    private String isbn;
    private Integer priceStandard;
    private Integer priceSales;
    private List<String> authorNames;
    private String publisherName;
    private String imageUrl;
    private Boolean liked;

    private Integer rank;
    private Double relevanceScore;
    private boolean recommended;

    private String llmSummary;


    public static BookSearchListResponse from(Book book, boolean liked) {
        return BookSearchListResponse.builder()
            .id(book.getId())
            .title(book.getTitle())
            .isbn(book.getIsbn())
            .priceStandard(book.getPriceStandard())
            .priceSales(book.getPriceSales())
            .authorNames(book.getBookAuthorList().stream().map(BookAuthor::getAuthor).map(
                Author::getName).toList())
            .publisherName(book.getPublisher() != null ? book.getPublisher().getName() : "출판사 정보 없음")
            .imageUrl(book.getBookImageList().stream().filter(i -> i.getImageType().equals(
                ImageType.THUMBNAIL)).findFirst().map(BookImage::getImageUrl).orElse(null))
            .liked(liked)
            .build();
    }
}
