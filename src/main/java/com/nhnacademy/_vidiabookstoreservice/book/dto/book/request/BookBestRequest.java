package com.nhnacademy._vidiabookstoreservice.book.dto.book.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BookBestRequest {

    List<Long> bookIdList;


}
