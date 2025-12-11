package com.nhnacademy._vidiabookstoreservice.book.service.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.book.config.GeminiProperties;
import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiBookSuggestion;
import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiContent;
import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiGenerationConfig;
import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiPart;
import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class GeminiAnswerService {

    private final RestClient geminiRestClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    public List<GeminiBookSuggestion> generateSuggestions(String userQuestion, List<BookDocument> rerankedDocs) {

        String prompt = buildPrompt(userQuestion, rerankedDocs);

        GeminiRequest request = new GeminiRequest(
            List.of(new GeminiContent(List.of(new GeminiPart(prompt)))),
            new GeminiGenerationConfig(
                0.7,
                0.8,
                List.of("\n\n", "---")
            )
        );

        GeminiResponse response = geminiRestClient.post()
            .uri("/models/" + geminiProperties.getModel() + ":generateContent")
            .body(request)
            .retrieve()
            .body(GeminiResponse.class);

        if (response == null
            || response.candidates() == null
            || response.candidates().isEmpty()
            || response.candidates().get(0).content() == null
            || response.candidates().get(0).content().parts().isEmpty()) {
            return List.of();
        }

        String json = response.candidates()
            .get(0)
            .content()
            .parts()
            .get(0)
            .text();

        try {
            return objectMapper.readValue(
                json,
                new TypeReference<List<GeminiBookSuggestion>>() {}
            );
        } catch (Exception e) {
            // JSON 형식 안 지킨 경우 대비
            // 로그 찍고 그냥 빈 리스트 반환
            // log.warn("Failed to parse Gemini JSON: {}", json, e);
            return List.of();
        }

}

    private String buildPrompt(String userQuestion, List<BookDocument> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 사용자의 도서 검색 질의와, 검색/재순위화된 도서 목록입니다.\n")
            .append("각 도서에는 내부 식별자 bookId가 있습니다.\n")
            .append("당신의 역할은, 이 목록 중에서 질문에 가장 잘 맞는 책들을 고르고, ")
            .append("관련성이 높은 순으로 rank를 매기고, 각 책별 추천 이유(summary)를 만드는 것입니다. 이유는 가능한 길게 만드는데 너무 과하지는 않고 책의 특징을 잘 잡아서 만들어주시면 좋겠습니다.\n\n")

            .append("반드시 아래 JSON 형식만 출력하세요. 추가 설명/자연어 문장은 절대 쓰지 마세요.\n")
            .append("형식 예시:\n")
            .append("[\n")
            .append(
                "  {\"bookId\": 31518, \"rank\": 1, \"relevanceScore\": 0.95, \"recommended\": true, \"summary\": \"...\"},\n")
            .append(
                "  {\"bookId\": 12345, \"rank\": 2, \"relevanceScore\": 0.87, \"recommended\": true, \"summary\": \"...\"}\n")
            .append("]\n\n")

            .append("필드 설명:\n")
            .append("- bookId: 아래 목록에 있는 책의 id 그대로 사용\n")
            .append("- rank: 1부터 시작하는 정수, 관련성 높은 책일수록 작은 숫자\n")
            .append("- relevanceScore: 0~1 사이 부동소수점, 관련성 추정 점수\n")
            .append("- recommended: true/false, 사용자가 볼 만한 책이면 true\n")
            .append("- summary: 한국어로 간단한 추천 이유\n\n")

            .append("---\n")
            .append("사용자 질문: ").append(userQuestion).append("\n\n")
            .append("검색 결과:\n");

        int rank = 1;
        for (BookDocument doc : docs) {
            sb.append(rank++).append(". id: ").append(doc.getId()).append("\n")
                .append("   제목: ").append(doc.getTitle()).append("\n")
                .append("   설명: ").append(doc.getDescription()).append("\n")
                .append("   저자: ").append(doc.getAuthors()).append("\n")
                .append("   평점: ").append(doc.getRating()).append("\n\n");
        }

        return sb.toString();
    }

}
