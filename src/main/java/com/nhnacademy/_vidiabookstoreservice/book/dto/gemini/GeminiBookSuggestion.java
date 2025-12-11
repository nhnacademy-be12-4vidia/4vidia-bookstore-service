package com.nhnacademy._vidiabookstoreservice.book.dto.gemini;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeminiBookSuggestion {
    private Long bookId;
    private Integer rank;
    private Double relevanceScore;
    private boolean recommended;
    private String summary;
}
