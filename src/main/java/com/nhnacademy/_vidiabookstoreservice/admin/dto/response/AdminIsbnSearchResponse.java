package com.nhnacademy._vidiabookstoreservice.admin.dto.response;

import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorNameRoleResponse;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public record AdminIsbnSearchResponse(

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
        //Integer stock,
        //Boolean packagingAvailable,

        String description,
        String bookIndex,
        List<String> tags
) {
}