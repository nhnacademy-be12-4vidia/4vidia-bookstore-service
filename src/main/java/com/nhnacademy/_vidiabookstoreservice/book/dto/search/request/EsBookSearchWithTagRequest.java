package com.nhnacademy._vidiabookstoreservice.book.dto.search.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EsBookSearchWithTagRequest {
    private List<String> tagNameList;
    private MatchMode mode = MatchMode.OR;

    public enum MatchMode {AND, OR}
}
