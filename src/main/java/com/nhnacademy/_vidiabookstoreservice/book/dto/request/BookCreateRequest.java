package com.nhnacademy._vidiabookstoreservice.book.dto.request;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
public class BookCreateRequest {

    @NotBlank(message = "ISBN은 필수입니다.")
    private String isbn;

    @NotBlank(message = "도서 제목은 필수입니다.")
    private String title;

    private String subtitle;

    private String bookIndex;

    private String description;

    @NotBlank(message = "출판사는 필수입니다.")
    private String publisherName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishedDate;

    private Integer priceStandard;

    @NotBlank(message = "저자는 필수입니다.")
    private String authorList;

    private String contributorList;

    private String volumeNumber;

    private String imageUrl;

    public Book toEntity(Publisher publisher) {
        return Book.builder()
            .isbn(this.isbn)
            .title(this.title)
            .subtitle(this.subtitle)
            .bookIndex(this.bookIndex)
            .description(this.description)
            .publisher(publisher)
            .publishedDate(this.publishedDate)
            .priceStandard(this.priceStandard)
            .volumeNumber(parseIntegerSafe(this.volumeNumber))
            .build();
    }

    private Integer parseIntegerSafe(String value) {
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
