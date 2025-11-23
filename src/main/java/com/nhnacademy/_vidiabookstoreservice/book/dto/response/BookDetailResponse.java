package com.nhnacademy._vidiabookstoreservice.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
public class BookDetailResponse {

    @NotNull(message = "id는 필수입니다.")
    private Long id;

    @NotBlank(message = "ISBN은 필수입니다.")
    private String isbn;

    @NotBlank(message = "도서 제목은 필수입니다.")
    private String title;

    private String subtitle;

    private String index;

    private String description;

    @NotBlank(message = "출판사는 필수입니다.")
    private String publisherName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishedDate;

    private Integer pageCount;

    private String language;

    private Integer priceStandard;

    private Integer stock;

    private String stockStatus;

    private Boolean packagingAvailable;

    @NotBlank(message = "저자는 필수입니다.")
    private String authorList;

    private String volumeNumber;

    private String imageUrl;

}
