package com.nhnacademy._vidiabookstoreservice.book.dto.book.request;

import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.request.AuthorRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class BookUpdateRequest {

    @NotBlank(message = "도서 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "출판사는 필수입니다.")
    private String publisherName;

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    private List<AuthorRequest> authorList;

    @NotNull(message = "판매 상태는 필수입니다.")
    private StockStatus stockStatus;

    @Min(0)
    private Integer priceStandard;

    @Min(0)
    private Integer stock;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishedDate;

    private String thumbnailUrl;
    private String tagList;
    private String subtitle;
    private String bookIndex;
    private String description;

    private Integer pageCount;
    private String language;
    private String volumeNumber;
    private Boolean packagingAvailable;

}
