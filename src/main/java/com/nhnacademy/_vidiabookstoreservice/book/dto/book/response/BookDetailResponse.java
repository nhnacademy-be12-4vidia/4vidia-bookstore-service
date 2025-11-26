package com.nhnacademy._vidiabookstoreservice.book.dto.book.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookDetailResponse {

    private Long id;
    private String isbn;
    private String title;
    private String subtitle;
    private String bookIndex;
    private String description;

    private PublisherInfo publisher;
    private LocalDate publishedDate;

    private CategoryInfo category;

    private Integer pageCount;
    private String language;
    private Integer priceStandard;
    private Integer priceSales;
    private Integer stock;
    private String stockStatus;
    private boolean packagingAvailable;

    private List<AuthorInfo> authors;
    private Integer volumeNumber;
    private List<String> imageUrls;

    @Getter @Builder
    public static class PublisherInfo {
        private Long id;
        private String name;
    }

    @Getter @Builder
    public static class AuthorInfo {

        private Long id;
        private String name;
        private String role;
    }

    @Getter @Builder
    public static class CategoryInfo {

        private Long id;
        private String name;
        private String kdcCode;
    }

    public static BookDetailResponse from(Book book) {
        return BookDetailResponse.builder()
            .id(book.getId())
            .isbn(book.getIsbn())
            .title(book.getTitle())
            .subtitle(book.getSubtitle())
            .bookIndex(book.getBookIndex())
            .description(book.getDescription())
            .publisher(book.getPublisher() != null ? PublisherInfo.builder()
                .id(book.getPublisher().getId())
                .name(book.getPublisher().getName())
                .build() : null)
            .category(book.getCategory() != null ? CategoryInfo.builder()
                .id(book.getCategory().getId())
                .name(book.getCategory().getCategoryName())
                .kdcCode(book.getCategory().getKdcCode())
                .build() : null)
            .publishedDate(book.getPublishedDate())
            .pageCount(book.getPageCount())
            .language(book.getLanguage())
            .priceStandard(book.getPriceStandard())
            .priceSales(book.getPriceSales())
            .stock(book.getStock())
            .stockStatus(book.getStockStatus().name())
            .packagingAvailable(book.isPackagingAvailable())
            .authors(book.getBookAuthors().stream().map(ba -> AuthorInfo.builder()
                .id(ba.getAuthor().getId())
                .name(ba.getAuthor().getName())
                .role(ba.getRole())
                .build()).toList())
            .volumeNumber(book.getVolumeNumber())
            .imageUrls(book.getBookImageList().stream().map(BookImage::getImageUrl).toList())
            .build();
    }

}
