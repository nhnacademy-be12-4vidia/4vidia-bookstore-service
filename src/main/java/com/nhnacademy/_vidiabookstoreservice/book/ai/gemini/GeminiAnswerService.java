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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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
        List<String> keyList = geminiProperties.getApiKeyList();
        int keyCount = (keyList == null) ? 0 : keyList.size();

        List<String> previews = new ArrayList<>();
        if (keyList != null) {
            for (int i = 0; i < Math.min(3, keyCount); i++) {
                previews.add(mask(keyList.get(i)));
            }
        }

        log.info("[LLM-CONFIG] gemini model = {}, apiKeyCount = {}, apiKeyPreviews = {}",
                geminiProperties.getModel(),
                keyCount,
                previews);
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
//        dumpJsonToProjectFile("gemini_search", json);
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
             log.warn("Failed to parse Gemini JSON: {}", json, e);
            return List.of();
        }
    }

    public String summarizeReview(Long bookId, String reviewTextBundle) {
        String prompt = buildReviewSummaryPrompt(bookId, reviewTextBundle);

        String raw = getGeminiText(buildGeminiRequest(prompt, 0.2, 0.8))
                .map(String::trim)
                .orElse("");

        if (raw.isBlank()) return "";

        String json = extractFirstJsonObjectOrNull(raw);
        if (json == null) {
            log.warn("[LLM-REVIEW] Non-JSON response. rawPreview={}", raw.length() > 300 ? raw.substring(0, 300) + "...<truncated>" : raw);
            return "";
        }

        try {
            var node = objectMapper.readTree(json);
            String summary = node.path("reviewSummary").asText("").trim();
            String recommendedFor = node.path("recommendedFor").asText("").trim();

            if (summary.isBlank() && recommendedFor.isBlank()) {
                return "";
            }

            if (recommendedFor.isBlank()) {
                return "리뷰요약: " + summary;
            }
            return "리뷰요약: " + summary + "\n" + "추천대상: " + recommendedFor;

        } catch (Exception e) {
            log.warn("[LLM-REVIEW] Failed to parse review JSON. jsonPreview={}", json.length() > 500 ? json.substring(0, 500) + "...<truncated>" : json, e);
            return "";
        }
    }

    private String buildSearchPrompt(String userQuestion, List<BookDocument> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 사용자의 도서 검색 질의와, 검색/재순위화된 도서 목록입니다.\n")
            .append("각 도서에는 내부 식별자 bookId가 있습니다.\n")
            .append("당신의 역할은, 이 목록 중에서 질문에 가장 잘 맞는 책들을 고르고, ")
            .append("관련성이 높은 순으로 rank를 매기고, 각 책별 추천 이유(summary)를 만드는 것입니다. 이유는 가능한 길게 만드는데 너무 과하지는 않고 책의 특징을 잘 잡아서 만들어주시면 좋겠습니다.\n\n")
                .append("설명이 없는 도서는 우선순위를 낮춰주시기 바랍니다.\n")
                .append("프로그래밍과 관련된 도서의 우선순위는 높여주시기 바랍니다.\n")
                .append("목록에 없는 bookId는 절대 만들지 마세요.\n")

                .append("반드시 '유효한 JSON'만 출력하세요. JSON 이외의 글자(설명/자연어/코드블록/마크다운/백틱/괄호/접두사/접미사)를 절대 출력하지 마세요.\n")
                .append("특히 매우 중요: 아래 문자를 절대 출력하지 마세요: ``` , ` (백틱), json (코드펜스 언어표시), Markdown 형식\n")
                .append("출력은 반드시 '[' 로 시작해서 ']' 로 끝나야 합니다. (앞뒤 공백/개행 포함 금지)\n")
                .append("반드시 JSON 배열(array)만 반환하세요. (객체 단독 반환 금지)\n")
                .append("문법 규칙: 키는 큰따옴표(\")를 사용, 문자열도 큰따옴표 사용, 마지막 요소에 트레일링 콤마 금지\n")
                .append("값 규칙: bookId/rank는 정수, relevanceScore는 0~1 실수, recommended는 true/false, summary는 한국어 문자열\n")
                .append("검증 규칙: 첫 글자가 '[' 가 아니면 실패이며, 마지막 글자가 ']' 가 아니면 실패입니다. 실패 시 반드시 [] 만 출력하세요.\n")
                .append("만약 출력에 ``` 또는 ` 또는 어떤 자연어 설명이 섞이려 한다면, 즉시 출력을 [] 로 바꾸세요.\n\n")

                .append("JSON 형식 예시(그대로 따라하세요, 코드펜스 금지):\n")
                .append("[\n")
                .append("  {\"bookId\":31518,\"rank\":1,\"relevanceScore\":0.95,\"recommended\":true,\"summary\":\"...\"},\n")
                .append("  {\"bookId\":12345,\"rank\":2,\"relevanceScore\":0.87,\"recommended\":true,\"summary\":\"...\"}\n")
                .append("]\n\n")

            .append("필드 설명:\n")
            .append("- bookId: 아래 목록에 있는 책의 id 그대로 사용\n")
            .append("- rank: 1부터 시작하는 정수, 관련성 높은 책일수록 작은 숫자\n")
            .append("- relevanceScore: 0~1 사이 부동소수점, 관련성 추정 점수\n")
            .append("- recommended: true/false, 사용자가 볼 만한 책이면 true\n")
            .append("- summary: 한국어로 추천 이유(2~4문장, 과장/추측 금지)\n\n")

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
          .append("리뷰는 사용자가 작성한 원문이며, 같은 내용이 반복되거나 잡음(광고/무의미한 문장/이모지/단답)이 있을 수 있습니다.\n\n")

          .append("요약 규칙:\n")
          .append("1. 리뷰 원문에 근거해서만 작성하고, 과장/추측/새 정보 생성은 금지합니다.\n")
          .append("2. 반복적으로 언급되는 장점/단점/분위기/추천 포인트를 우선해서 묶어 정리합니다.\n")
          .append("3. 숫자/사실/인용은 리뷰에 명시된 경우에만 포함합니다.\n")
          .append("4. 욕설/개인정보/비속어는 제거하거나 순화합니다.\n\n")

          .append("출력 형식 규칙(매우 중요):\n")
          .append("- 반드시 '유효한 JSON'만 출력하세요. JSON 이외의 글자(설명/자연어/코드블록/마크다운/백틱/괄호/접두사/접미사)를 절대 출력하지 마세요.\n")
          .append("- 출력은 반드시 '{' 로 시작해서 '}' 로 끝나야 합니다. (앞뒤 공백/개행 포함 금지)\n")
          .append("- 반드시 JSON 객체(object)만 반환하세요.\n")
          .append("- 문법: 키는 큰따옴표(\") 사용, 문자열도 큰따옴표 사용, 트레일링 콤마 금지\n")
          .append("- 규칙을 지키기 어렵다면 아래 빈 객체만 출력하세요: {}\n\n")

          .append("반환 JSON 스키마(키 이름 정확히 지키기):\n")
          .append("{\n")
          .append("  \"reviewSummary\": \"2~5문장 한 단락 요약(한국어)\",\n")
          .append("  \"recommendedFor\": \"한 줄 추천 대상(한국어)\"\n")
          .append("}\n\n")

          .append("작성 가이드:\n")
          .append("- reviewSummary는 2~5문장으로 자연스럽게 연결된 한 단락으로 작성하세요.\n")
          .append("- recommendedFor는 한 줄로, 어떤 독자에게 맞는지 간결하게 작성하세요.\n\n")

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

        int keyCount = 0;
        try {
            List<String> keyList = geminiProperties.getApiKeyList();
            keyCount = (keyList == null) ? 0 : keyList.size();
        } catch (Exception ignored) {}

        int maxAttempts = keyCount == 0 ? 1 : keyCount;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                GeminiResponse response = getGeminiResponse(request);
                return extractFirstText(response);
            } catch (HttpClientErrorException e) {
                String body = safeBody(e);
                boolean retryableQuota = isQuotaOrRateLimit(e, body);

                log.error("[LLM-CALL] Gemini failed. attempt = {}/{}, status = {}, retryableQuota = {}, body = {}", attempt, maxAttempts, e.getStatusCode(), retryableQuota, body);

                if (retryableQuota && attempt < maxAttempts) {
                    sleepSilently(250L * attempt);
                    continue;
                }

                return Optional.empty();
            } catch (Exception e) {
                log.error("[LLM-CALL] Gemini failed. attempt = {}/{}, exClass = {}, msg = {}", attempt, maxAttempts, e.getClass().getName(), e.getMessage(), e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private boolean isQuotaOrRateLimit(HttpClientErrorException e, String body) {
        int code = e.getStatusCode().value();
        if (code == 429 || code == 503) return true;
        if (body == null) return false;
        String b = body.toLowerCase();
        return b.contains("resource_exhausted")
                || b.contains("quota")
                || b.contains("rate")
                || b.contains("too many requests")
                || b.contains("exceeded")
                || b.contains("limit");
    }

    private String safeBody(HttpClientErrorException e) {
        try {
            String body = e.getResponseBodyAsString();
            if (body == null) return "<null>";
            return body.length() > 1500 ? body.substring(0, 1500) + "...<truncated>" : body;
        } catch (Exception exception) {
            return "<unavailable>";
        }
    }

    private void sleepSilently(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
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

    private String extractFirstJsonObjectOrNull(String raw) {
        if (raw == null) return null;
        int start = raw.indexOf('{');
        if (start < 0) return null;

        int depth = 0;
        for (int i = start; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return raw.substring(start, i + 1).trim();
                }
            }
        }
        return null;
    }
//
//    private void dumpJsonToProjectFile(String prefix, String json) {
//        if (json == null) return;
//
//        try {
//            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
//
//            Path dir = Paths.get(System.getProperty("user.dir"), "llm-dumps");
//            Files.createDirectories(dir);
//
//            Path file = dir.resolve(prefix + "_" + ts + ".txt");
//            Files.writeString(file, json, StandardCharsets.UTF_8);
//
//            log.info("[LLM-DUMP] saved: {}", file.toAbsolutePath());
//        } catch (Exception e) {
//            log.warn("[LLM-DUMP] failed to save json. msg={}", e.getMessage(), e);
//        }
//        }

}
