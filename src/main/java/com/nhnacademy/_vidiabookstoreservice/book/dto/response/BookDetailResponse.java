package com.nhnacademy._vidiabookstoreservice.book.dto.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Builder
public class BookDetailResponse {

    private Long id;
    private String isbn;
    private String title;
    private String subtitle;
    private String bookIndex;
    private String description;

    private String publisherName;
    private LocalDate publishedDate;

    private Integer pageCount;
    private String language;
    private Integer priceStandard;
    private Integer priceSales;
    private Integer stock;
    private String stockStatus;
    private boolean packagingAvailable;

    private List<String> authors;
    private Integer volumeNumber;
    private String imageUrl;

    public static BookDetailResponse from(Book book) {
        return BookDetailResponse.builder()
            .id(book.getId())
            .isbn(book.getIsbn())
            .title(book.getTitle())
            .subtitle(book.getSubtitle())
            .bookIndex(book.getBookIndex())
            .description(book.getDescription())
            .publisherName(book.getPublisher() != null ? book.getPublisher().getName() : "")
            .publishedDate(book.getPublishedDate())
            .pageCount(book.getPageCount())
            .language(book.getLanguage())
            .priceStandard(book.getPriceStandard())
            .priceSales(book.getPriceSales())
            .stock(book.getStock())
            .stockStatus(book.getStockStatus().name())
            .packagingAvailable(book.isPackagingAvailable())
            .volumeNumber(book.getVolumeNumber())

            .authors(book.getBookAuthors().stream()
                .map(ba -> ba.getAuthor().getName())
                .toList())
            .build();
    }

}
