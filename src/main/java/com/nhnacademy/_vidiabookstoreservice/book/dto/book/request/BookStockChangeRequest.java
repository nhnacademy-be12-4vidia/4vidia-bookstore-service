package com.nhnacademy._vidiabookstoreservice.book.dto.book.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookStockChangeRequest {

    Long bookId;
    Integer quantity;

}
