package com.nhnacademy._vidiabookstoreservice.book.dto.gemini;

import java.util.List;

public record GeminiRequest(List<GeminiContent> contents,
                            GeminiGenerationConfig generationConfig) {

}
