package com.nhnacademy._vidiabookstoreservice.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorNameRoleResponse;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS // 직렬화 시 클래스 정보를 포함
)
public record AdminIsbnSearchResponse(

        boolean found,
        Long bookId,
        String isbn,

        String coverImageUrl,
        String title,
        String subtitle,
        List<AuthorNameRoleResponse> authors,
        String publisher,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate publishedDate,

        String language,
        Integer pageCount,
        String categoryCode,

        Integer priceStandard,
        Integer stock,
        StockStatus stockStatus,
        Boolean packagingAvailable,

        String description,
        String bookIndex,
        List<String> tags
) {
    // 1) DB에서 찾음
    public static AdminIsbnSearchResponse foundFromDb(Book book) {
        // 썸네일 이미지 찾기 (없으면 null)
        String coverImageUrl = book.getBookImageList().stream()
                .filter(img -> img.getImageType() != null && img.getImageType().name().equals("THUMBNAIL"))
                .findFirst()
                .map(BookImage::getImageUrl)
                .orElse(null);

        // 저자 목록 변환
        List<AuthorNameRoleResponse> authors = book.getBookAuthorList().stream()
                .map(ba -> new AuthorNameRoleResponse(ba.getAuthor().getName(), ba.getRole()))
                .toList();

        // 태그 목록 변환
        List<String> tags = book.getBookTagList().stream()
                .map(bt -> bt.getTag().getName())
                .toList();

        return new AdminIsbnSearchResponse(
                true,
                book.getId(),
                book.getIsbn(),
                coverImageUrl,
                book.getTitle(),
                book.getSubtitle(),
                authors,
                book.getPublisher() != null ? book.getPublisher().getName() : null,
                book.getPublishedDate(),
                book.getLanguage(),
                book.getPageCount(),
                book.getCategory() != null ? book.getCategory().getKdcCode() : null,
                book.getPriceStandard(),
                book.getStock(),
                book.getStockStatus(),
                book.isPackagingAvailable(),
                book.getDescription(),
                book.getBookIndex(),
                tags
        );
    }
}
