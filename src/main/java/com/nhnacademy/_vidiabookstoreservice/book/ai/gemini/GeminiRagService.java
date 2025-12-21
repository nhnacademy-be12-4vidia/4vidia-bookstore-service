package com.nhnacademy._vidiabookstoreservice.book.ai.gemini;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiRagService {

    private final RestClient geminiRestClient;

    public AdminIsbnSearchResponse augmentBookInfo(Object book) {

        return null;
    }
}
