package com.nhnacademy._vidiabookstoreservice.book.dto.search.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiBookSuggestion;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class AiCacheResponse {
    private Long id;
    private String title;
    private String isbn;
    private Integer priceStandard;
    private Integer priceSales;
    private List<String> authorNames;
    private String publisherName;
    private String imageUrl;

    private Integer rank;
    private Double relevanceScore;
    private boolean recommended;

    private String llmSummary;

    public static AiCacheResponse of(Book book, GeminiBookSuggestion suggestion) {
        return AiCacheResponse.builder()
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
                .rank(suggestion.getRank())
                .relevanceScore(suggestion.getRelevanceScore())
                .recommended(suggestion.isRecommended())
                .llmSummary(suggestion.getSummary())
                .build();
    }
}
