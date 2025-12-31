package com.nhnacademy._vidiabookstoreservice.book.dto.book.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookStockChangeRequest {

    Long bookId;
    Integer quantity;

}
