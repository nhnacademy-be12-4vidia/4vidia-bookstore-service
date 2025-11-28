package com.nhnacademy._vidiabookstoreservice.book.dto.book.request;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@NoArgsConstructor
public class BookCreateRequest {

    @NotBlank(message = "ISBN은 필수입니다.")
    private String isbn;

    @NotBlank(message = "도서 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "출판사는 필수입니다.")
    private String publisherName;

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "저자는 필수입니다.")
    private String authorList;

    @Min(0)
    private Integer priceStandard;

    @Min(0)
    private Integer priceSales;

    @Min(0)
    private Integer stock;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishedDate;

    private String contributorList;
    private String subtitle;
    private String bookIndex;
    private String description;

    private Integer pageCount;
    private String language;
    private String volumeNumber;
    private Boolean packagingAvailable;

    public Book toEntity(Publisher publisher, Category category) {
        return Book.builder()
            .isbn(this.isbn)
            .title(this.title)
            .subtitle(this.subtitle)
            .bookIndex(this.bookIndex)
            .description(this.description)
            .publisher(publisher)
            .category(category)
            .publishedDate(this.publishedDate)
            .priceStandard(this.priceStandard)
            .priceSales(this.priceSales)
            .stock(this.stock)
            .pageCount(this.pageCount)
            .language(this.language)
            .packagingAvailable(Boolean.TRUE.equals(this.packagingAvailable))
            .volumeNumber(parseIntegerSafe(this.volumeNumber))
            .build();
    }

    private Integer parseIntegerSafe(String value) {
        try {
            return value != null && !value.isBlank() ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
