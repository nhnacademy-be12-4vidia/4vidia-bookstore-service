package com.nhnacademy._vidiabookstoreservice.book.dto.gemini;

import java.util.List;

public record GeminiGenerationConfig(Double temperature,
                                     Double topP,
                                     List<String> stopSequences) {

}
