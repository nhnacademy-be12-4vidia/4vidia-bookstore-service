package com.nhnacademy._vidiabookstoreservice.book.dto.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookListResponse {

    private Long id;
    private String title;
    private String isbn;
    private Integer priceSales;
    private List<String> authorNames;
    private String publisherName;
    private String imageUrl;


    public static BookListResponse from(Book book) {
        return BookListResponse.builder()
            .id(book.getId())
            .title(book.getTitle())
            .isbn(book.getIsbn())
            .priceSales(book.getPriceSales())
            .authorNames(book.getBookAuthors().stream().map(BookAuthor::getAuthor).map(
                Author::getName).toList())
            .publisherName(book.getPublisher().getName())
            .imageUrl(book.getBookImageList().stream().filter(i -> i.getImageType().equals(
                ImageType.THUMBNAIL)).findFirst().map(BookImage::getImageUrl).orElse(null))
            .build();
    }
}
