package com.nhnacademy._vidiabookstoreservice.book.ai.gemini;

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
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAnswerService {

    private final RestClient geminiRestClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    @PostConstruct
    void logGeminiConfig() {
        String key = geminiProperties.getApiKey();
        log.info("[LLM-CONFIG] gemini model={}, apiKeyPresent={}, apiKeyLen={}, apiKeyPreview={}",
                geminiProperties.getModel(),
                hasText(key),
                key == null ? 0 : key.length(),
                mask(key));
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String mask(String s) {
        if (!hasText(s)) return "<empty>";
        int n = s.length();
        if (n <= 6) return "<too-short>";
        return s.substring(0, 2) + "****" + s.substring(n - 2);
    }

    public List<GeminiBookSuggestion> generateSuggestions(String userQuestion, List<BookDocument> rerankedDocs) {

        String prompt = buildSearchPrompt(userQuestion, rerankedDocs);

        String json = getGeminiText(buildGeminiRequest(prompt, 0.7, 0.8)).orElse(null);
        if (json == null || json.isBlank()) {
            return List.of();
        }

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

    public String summarizeReview(Long bookId, String reviewTextBundle) {
        String prompt = buildReviewSummaryPrompt(bookId, reviewTextBundle);

        return getGeminiText(buildGeminiRequest(prompt, 0.4, 0.8))
                .map(String::trim)
                .orElse("");

    }

    private String buildSearchPrompt(String userQuestion, List<BookDocument> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 사용자의 도서 검색 질의와, 검색/재순위화된 도서 목록입니다.\n")
            .append("각 도서에는 내부 식별자 bookId가 있습니다.\n")
            .append("당신의 역할은, 이 목록 중에서 질문에 가장 잘 맞는 책들을 고르고, ")
            .append("관련성이 높은 순으로 rank를 매기고, 각 책별 추천 이유(summary)를 만드는 것입니다. 이유는 가능한 길게 만드는데 너무 과하지는 않고 책의 특징을 잘 잡아서 만들어주시면 좋겠습니다.\n\n")
                .append("설명이 없는 도서는 우선순위를 낮춰주시기 바랍니다.\n")
                .append("프로그래밍과 관련된 도서의 우선순위는 높여주시기 바랍니다.\n")

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

    private String buildReviewSummaryPrompt(Long bookId, String reviewTextBundle) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 도서 리뷰 요약 도우미입니다.\n")
                .append("아래는 특정 도서의 리뷰 모음입니다. 도서 식별자는 bookId 입니다.\n")
                .append("리뷰는 사용자가 작성한 원문이며, 같은 내용이 반복되거나 잡음이 있을 수 있습니다.\n")
                .append("요약 시에는 사실에 근거해 과장하지 말고, 리뷰에서 반복적으로 등장하는 내용 위주로 정리하세요.\n\n")
                .append("리뷰요약은 최대한 길지만 사용자가 한눈에 읽기 쉬울만한 정도의 길이로 만들어주세요.")

                .append("반드시 아래 형식으로만 출력하세요(마크다운/추가 설명 금지).\n")
                .append("형식:\n")
                .append("리뷰요약: <한 줄>\n")
                .append("추천대상: <한 줄>\n\n")

                .append("bookId: ").append(bookId).append("\n")
                .append("---\n")
                .append("리뷰 원문:\n")
                .append(reviewTextBundle == null ? "" : reviewTextBundle);

        return sb.toString();
    }

    private GeminiRequest buildGeminiRequest(String prompt, double temperature, double topP) {
        return new GeminiRequest(
                List.of(new GeminiContent(List.of(new GeminiPart(prompt)))),
                new GeminiGenerationConfig(temperature, topP, List.of("\n\n", "---"))
        );
    }

    private Optional<String> getGeminiText(GeminiRequest request) {
//        GeminiResponse response = getGeminiResponse(request);
//        return extractFirstText(response);

        try {
            GeminiResponse response = getGeminiResponse(request);
            return extractFirstText(response);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("[LLM-CALL] Gemini failed. status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("[LLM-CALL] Gemini failed. exClass={}, msg={}",
                    e.getClass().getName(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Optional<String> extractFirstText(GeminiResponse response) {
        if (response == null
                || response.candidates() == null
                || response.candidates().isEmpty()
                || response.candidates().get(0).content() == null
                || response.candidates().get(0).content().parts().isEmpty()) {
            return Optional.empty();
        }

        String text = response.candidates()
                .get(0)
                .content()
                .parts()
                .get(0)
                .text();

        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(text);
    }

    private GeminiResponse getGeminiResponse(GeminiRequest request) {
        return geminiRestClient.post()
                .uri("/models/" + geminiProperties.getModel() + ":generateContent")
                .body(request)
                .retrieve()
                .body(GeminiResponse.class);
    }


}
