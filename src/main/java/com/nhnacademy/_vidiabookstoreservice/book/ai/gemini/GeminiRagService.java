package com.nhnacademy._vidiabookstoreservice.book.ai.gemini;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.aladin.client.AladinApiClient;
import com.nhnacademy._vidiabookstoreservice.book.aladin.dto.AladinItemDto;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorNameRoleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiRagService {

    @Qualifier("aladinRestClient")
    private final RestClient aladinRestClient;
    private final AladinApiClient aladinApiClient;
    private final RestClient geminiRagRestClient;
    private final ObjectMapper objectMapper;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    /**
     * 도서 정보 보강 처리 (엔티티 대신 DTO를 받아 연관관계 로딩 문제와 트랜잭션 점유 문제를 해결)
     *
     * @param isbn ISBN13
     * @param dbData DB에서 조회된 기존 데이터 (없으면 null)
     * @return 보강된 도서 정보
     */
    public AdminIsbnSearchResponse augmentBookInfo(String isbn, AdminIsbnSearchResponse dbData) {
        return processAugmentation(isbn, dbData);
    }

    private AdminIsbnSearchResponse processAugmentation(String isbn, AdminIsbnSearchResponse dbData) {

        // 1. 알라딘 API 호출
        AladinItemDto aladinItem = aladinApiClient.lookupByIsbn(
                aladinRestClient,
                isbn,
                aladinApiKey
        ).orElse(null);

        // 2. 프롬프트 생성 (DTO 정보 + 알라딘 정보 제공)
        String prompt = buildRagPrompt(isbn, dbData, aladinItem);

        // 3. GEMINI 호출 (검색 + 정보 병합)
        String geminiJson = callGeminiWithSearchTool(prompt);

        // 4. 결과 매핑 (DB 없더라도 알라딘/LLM 결합해 관리자에게 최대 정보 제공)
        return mapToResponse(geminiJson, dbData, aladinItem);
    }

    private String buildRagPrompt(String isbn, AdminIsbnSearchResponse dbData, AladinItemDto aladin) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 도서 데이터 통합 전문가입니다.\n");
        sb.append("아래 제공된 내부 DB 정보, 외부 API 정보 및 Google Search 결과를 하나로 합쳐 완벽한 도서 정보를 JSON으로 만드세요.\n\n");

        sb.append("[공통 정보]\n");
        sb.append("- ISBN: ").append(isbn).append("\n\n");

        sb.append("[1. 내부 DB 정보]\n");
        if (dbData != null) {
            sb.append("- 제목: ").append(nvl(dbData.title())).append("\n");
            sb.append("- 부제목: ").append(nvl(dbData.subtitle())).append("\n");
            sb.append("- 저자 목록: ").append(dbData.authors() != null && !dbData.authors().isEmpty() ? dbData.authors() : "정보 없음").append("\n");
            sb.append("- 출판사: ").append(nvl(dbData.publisher())).append("\n");
            sb.append("- 출판일: ").append(dbData.publishedDate() != null ? dbData.publishedDate() : "정보 없음").append("\n");
            sb.append("- 언어: ").append(nvl(dbData.language())).append("\n");
            sb.append("- 페이지 수: ").append(dbData.pageCount() != null ? dbData.pageCount() : "정보 없음").append("\n");
            sb.append("- 카테고리(kdc) 코드: ").append(nvl(dbData.categoryCode())).append("\n");
            sb.append("- 정가: ").append(dbData.priceStandard() != null ? dbData.priceStandard() : "정보 없음").append("\n");
            sb.append("- 설명: ").append(StringUtils.hasText(dbData.description()) ? "있음" : "정보 없음").append("\n");
            sb.append("- 목차: ").append(StringUtils.hasText(dbData.bookIndex()) ? "있음" : "정보 없음").append("\n");
        } else {
            sb.append("- 데이터 없음\n");
        }
        sb.append("\n");

        sb.append("[2. 외부 API(알라딘) 정보]\n");
        if (aladin != null) {
            sb.append("- 제목: ").append(nvl(aladin.title())).append("\n");
            sb.append("- 비정규 저자 필드: ").append(nvl(aladin.author())).append("\n");
            sb.append("- 출판일: ").append(nvl(aladin.pubDate())).append("\n");
            sb.append("- 설명: ").append(StringUtils.hasText(aladin.description()) ? "있음" : "정보 없음").append("\n");
            sb.append("- 정가: ").append(aladin.priceStandard() != null ? aladin.priceStandard() : "정보 없음").append("\n");
            sb.append("- 알라딘 카테고리 이름: ").append(nvl(aladin.categoryName())).append("\n");
            sb.append("- 출판사: ").append(nvl(aladin.publisher())).append("\n");
            if (aladin.bookinfo() != null) {
                sb.append("- 부제목: ").append(nvl(aladin.bookinfo().subTitle())).append("\n");
                sb.append("- 페이지 수: ").append(aladin.bookinfo().itemPage() != null ? aladin.bookinfo().itemPage() : "정보 없음").append("\n");
                sb.append("- 목차: ").append(StringUtils.hasText(aladin.bookinfo().toc()) ? "있음" : "정보 없음").append("\n");
                sb.append("- 정규 저자 필드: ")
                        .append((aladin.bookinfo().authors() != null && !aladin.bookinfo().authors().isEmpty()) ? aladin.bookinfo().authors() : "정보 없음")
                        .append("\n");
            }
        } else {
            sb.append("- 데이터 없음\n");
        }

        sb.append("\n[지시사항]\n");
        sb.append("1. **Google Search**를 반드시 사용하여 위 정보 중 '정보 없음'이거나 내용이 부실한 필드를 보강하세요.\n");
        sb.append("1-1. Google Search 결과에서 출판사에서 제공하는 정보를 우선 활용하고, 온라인 서점과 도서 정보 사이트 등을 교차해서 사용하세요.\n");
        sb.append("2. 특히 **'목차(bookIndex)'**와 **'상세 설명(description)'**은 반드시 검색을 통해 풍부하게 작성해야 합니다.\n");
        sb.append("2-1. 목차는 도서의 실제 목차를 전부 가져와야합니다. 획득한 도서 목차의 정보가 잘리지는 않았는지 확인해야 합니다.\n");
        sb.append("2-2. 상세 설명은 도서의 핵심 내용을 요약하고, 가능한 한 풍부하게 작성하세요.\n");
        sb.append("3. 저자 정보는 '이름'과 '역할(지은이/옮긴이/그림 등)'을 정확히 구분하여 리스트로 만드세요\n");
        sb.append("3-1. 여러명의 저자를 모두 포함하고, 역할이 불분명한 경우 역할은 비워두세요.\n");
        sb.append("3-2. 저자의 역할이 2개 이상인 경우, 정규화하여 (이름, 역할1), (이름, 역할2) 형태로 각각 추가하세요.\n");
        sb.append("4. categoryCode는 KDC 코드 세자리를 사용하고, 소수점은 사용하지 않습니다. 예: '005', '813'\n");
        sb.append("4-1. 해당 도서에 대한 kdc 정보를 찾지 못한 경우 'UNC'로 기재하세요.\n");
        sb.append("5. 언어는 해당 도서가 작성된 언어로 영어 2글자 소문자를 사용하세요. 예: 'ko', 'en', 'jp'\n");
        sb.append("6. 태그 목록은 알라딘 카테고리 이름을 > 구분자로 분리해 사용 할 것입니다.\n");
        sb.append("6-1. 알라딘 카테고리 이름과 중복되지 않게, 책에 대한 적절한 태그를 생성해주세요.\n");
        sb.append("6-2. 태그에는 출판사 이름이나 작가 이름을 포함하지 말아주세요\n");
        sb.append("7. 페이지 수(pageCount)와 정가(priceStandard)는 반드시 정수로 기재하세요.\n");
        sb.append("8. 모든 필드를 최대한 채우되, 불확실한 정보이거나 찾을 수 없는 정보는 절대 추가하지 마세요.\n");
        sb.append("8-1. 응답은 반드시 마크다운 코드 블록 없이 **순수 JSON 객체**로만 답변하세요.\n");

        sb.append("\n[목표 JSON 구조]\n");
        sb.append("{\n");
        sb.append("  \"title\": \"도서 제목\",\n");
        sb.append("  \"subtitle\": \"부제 (없으면 빈문자열)\",\n");
        sb.append("  \"authors\": [ {\"name\": \"저자명\", \"role\": \"역할\"} ],\n");
        sb.append("  \"publisher\": \"출판사명\",\n");
        sb.append("  \"publishedDate\": \"YYYY-MM-DD\",\n");
        sb.append("  \"language\": \"언어(ko, en...)\",\n");
        sb.append("  \"pageCount\": 페이지수,\n");
        sb.append("  \"categoryCode\": \"카테고리 코드\",\n");
        sb.append("  \"priceStandard\": 0,\n");
        sb.append("  \"description\": \"상세 설명 (최대한 풍부하게)\",\n");
        sb.append("  \"bookIndex\": \"목차 (줄바꿈 포함)\",\n");
        sb.append("  \"tags\": [\"태그1\", \"태그2\"]\n");
        sb.append("}");

        return sb.toString();
    }

    private String nvl(String s) {
        return StringUtils.hasText(s) ? s : "정보 없음";
    }

    private AdminIsbnSearchResponse mapToResponse(
            String json,
            AdminIsbnSearchResponse dbData,
            AladinItemDto aladin
    ) {
        if (!StringUtils.hasText(json)) {
            return fallbackResponse(dbData, aladin);
        }

        try {
            String cleaned = json.replace("```json", "").replace("```", "").trim();
            String extracted = extractFirstJsonObjectOrNull(cleaned);
            if (!StringUtils.hasText(extracted)) {
                log.warn("[관리자 도서] gemini 응답에서 JSON 추출 실패. preview={}", preview(cleaned));
                return fallbackResponse(dbData, aladin);
            }

            JsonNode root = objectMapper.readTree(extracted);

            List<AuthorNameRoleResponse> authors = parseAuthors(root.get("authors"), dbData, aladin);
            List<String> tags = parseTags(root.get("tags"), aladin, dbData);
            LocalDate publishedDate = parseLocalDateFlexible(root.path("publishedDate").asText(), dbData);

            Integer stock = (dbData != null) ? dbData.stock() : 0;
            String coverUrl = (aladin != null && StringUtils.hasText(aladin.cover()))
                    ? aladin.cover()
                    : (dbData != null ? dbData.coverImageUrl() : null);

            String title = firstNonEmpty(
                    root.path("title").asText(null),
                    dbData != null ? dbData.title() : null,
                    aladin != null ? aladin.title() : null,
                    "제목 없음");

            String subtitle = firstNonEmpty(
                    root.path("subtitle").asText(null),
                    dbData != null ? dbData.subtitle() : null,
                    aladin != null && aladin.bookinfo() != null ? aladin.bookinfo().subTitle() : null);

            String publisher = firstNonEmpty(
                    root.path("publisher").asText(null),
                    dbData != null ? dbData.publisher() : null,
                    aladin != null ? aladin.publisher() : null);

            String language = firstNonEmpty(
                    root.path("language").asText(null),
                    dbData != null ? dbData.language() : null);

            Integer pageCount = pickInteger(
                    parseIntegerFlexible(root.get("pageCount")),
                    dbData != null ? dbData.pageCount() : null,
                    (aladin != null && aladin.bookinfo() != null) ? aladin.bookinfo().itemPage() : null,
                    0
            );

            String categoryCode = firstNonEmpty(
                    root.path("categoryCode").asText(null),
                    dbData != null ? dbData.categoryCode() : null,
                    "UNC");

            Integer priceStandard = pickInteger(
                    parseIntegerFlexible(root.get("priceStandard")),
                    dbData != null ? dbData.priceStandard() : null,
                    aladin != null ? aladin.priceStandard() : null,
                    0
            );

            String description = firstNonEmpty(
                    dbData != null ? dbData.description() : null,
                    root.path("description").asText(null),
                    aladin != null ? aladin.description() : null,
                    "");

            String bookIndex = firstNonEmpty(
                    root.path("bookIndex").asText(null),
                    dbData != null ? dbData.bookIndex() : null,
                    (aladin != null && aladin.bookinfo() != null) ? aladin.bookinfo().toc() : null,
                    "");

            return new AdminIsbnSearchResponse(
                    true,
                    dbData != null ? dbData.bookId() : null,
                    coverUrl,
                    title,
                    subtitle,
                    authors,
                    publisher,
                    publishedDate,
                    language,
                    pageCount,
                    categoryCode,
                    priceStandard,
                    stock,
                    description,
                    bookIndex,
                    tags
            );

        } catch (Exception e) {
            log.warn("[관리자 도서] gemini 파싱 실패", e);
            return fallbackResponse(dbData, aladin);
        }
    }

    private AdminIsbnSearchResponse createEmptyResponse() {
        return new AdminIsbnSearchResponse(false, null, null, "검색 결과 없음", null, List.of(), null, null, null, 0, null, 0, 0, null, null, List.of());
    }

    private AdminIsbnSearchResponse fallbackResponse(AdminIsbnSearchResponse dbData, AladinItemDto aladin) {
        if (dbData != null) {
            return dbData;
        }
        if (aladin != null) {
            return new AdminIsbnSearchResponse(
                    true,
                    null,
                    aladin.cover(),
                    firstNonEmpty(aladin.title(), "제목 없음"),
                    aladin.bookinfo() != null ? aladin.bookinfo().subTitle() : null,
                    parseAuthors(null, null, aladin),
                    aladin.publisher(),
                    parseLocalDateFlexible(aladin.pubDate(), null),
                    null,
                    aladin.bookinfo() != null ? aladin.bookinfo().itemPage() : 0,
                    null,
                    aladin.priceStandard(),
                    0,
                    aladin.description(),
                    aladin.bookinfo() != null ? aladin.bookinfo().toc() : null,
                    parseTags(null, aladin, null)
            );
        }
        return createEmptyResponse();
    }

    private String preview(String s) {
        if (s == null) return "<null>";
        if (s.length() <= 200) return s;
        return s.substring(0, 200) + "...<truncated>";
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

    private List<AuthorNameRoleResponse> parseAuthors(JsonNode authorsNode, AdminIsbnSearchResponse dbData, AladinItemDto aladin) {
        List<AuthorNameRoleResponse> authors = new ArrayList<>();
        if (authorsNode != null && !authorsNode.isMissingNode() && !authorsNode.isNull()) {
            try {
                // 정상 객체 배열 케이스
                authors = objectMapper.convertValue(authorsNode, new TypeReference<>() {});
            } catch (Exception ignored) {
                // 문자열 배열일 경우 name만 채우기
                if (authorsNode.isArray()) {
                    for (JsonNode n : authorsNode) {
                        if (n.isTextual() && StringUtils.hasText(n.asText())) {
                            authors.add(new AuthorNameRoleResponse(n.asText(), null));
                        }
                    }
                }
            }
        }

        // 아무 것도 못 읽었을 때 정책: 비정규 저자 필드를 이름으로, 역할 null
        if (authors.isEmpty()) {
            // DB author 유지 우선
            if (dbData != null && dbData.authors() != null && !dbData.authors().isEmpty()) {
                return dbData.authors();
            }
            // aladin 비정규 필드 사용
            if (aladin != null && StringUtils.hasText(aladin.author())) {
                authors.add(new AuthorNameRoleResponse(aladin.author(), null));
            }
            if (authors.isEmpty()) {
                return List.of();
            }
        }
        return authors;
    }

    private List<String> parseTags(JsonNode tagsNode, AladinItemDto aladin, AdminIsbnSearchResponse dbData) {
        Set<String> set = new LinkedHashSet<>();
        // 1) Gemini 추천 태그
        if (tagsNode != null && !tagsNode.isMissingNode() && !tagsNode.isNull()) {
            try {
                List<String> geminiTags = objectMapper.convertValue(tagsNode, new TypeReference<>() {});
                for (String t : geminiTags) {
                    if (StringUtils.hasText(t)) set.add(t.trim());
                }
            } catch (Exception ignored) {}
        }
        // 2) Gemini 태그가 없으면 알라딘 카테고리 태그 사용, 단 Gemini 태그가 있더라도 추가 병합
        if (aladin != null && StringUtils.hasText(aladin.categoryName())) {
            for (String cat : aladin.categoryName().split(">")) {
                if (StringUtils.hasText(cat)) set.add(cat.trim());
            }
        }
        // 3) 그래도 비어있으면 DB 태그 사용
        if (set.isEmpty() && dbData != null && dbData.tags() != null) {
            set.addAll(dbData.tags());
        }
        return new ArrayList<>(set);
    }

    private LocalDate parseLocalDateFlexible(String value, AdminIsbnSearchResponse dbData) {
        if (!StringUtils.hasText(value)) {
            return dbData != null ? dbData.publishedDate() : null;
        }
        String normalized = value.replace(".", "-").replace("/", "-");
        String[] patterns = {"uuuu-MM-dd", "uuuu-M-d", "uuuu-MM", "uuuu-M"};
        for (String p : patterns) {
            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern(p)
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .toFormatter()
                        .withResolverStyle(ResolverStyle.STRICT);
                return LocalDate.parse(normalized, formatter);
            } catch (Exception ignored) {}
        }
        log.warn("[관리자 도서] gemini 날짜 파싱 실패: {}", value);
        return dbData != null ? dbData.publishedDate() : null;
    }

    private Integer parseIntegerFlexible(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        if (node.isInt() || node.isLong()) return node.asInt();
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    @SafeVarargs
    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (StringUtils.hasText(v)) return v;
        }
        return null;
    }

    private Integer pickInteger(Integer... values) {
        for (Integer v : values) {
            if (v != null) return v;
        }
        return null;
    }

    private String callGeminiWithSearchTool(String prompt) {
        try {
            Map<String, Object> request = Map.of(
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text", prompt))
                    )),
                    "tools", List.of(Map.of("google_search", Map.of()))
            );

            String responseBody = geminiRagRestClient.post()
                    .uri("/models/gemini-2.5-flash:generateContent")
                    .body(request)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || candidates.isEmpty()) {
                log.warn("[관리자 도서] Gemini returned no candidates. Response: {}", responseBody);
                return null;
            }

            // 여러 candidates / 여러 parts 고려: text 파트 이어붙이기
            StringBuilder sb = new StringBuilder();
            for (JsonNode candidate : candidates) {
                JsonNode content = candidate.path("content");
                if (content.isMissingNode()) continue;
                JsonNode parts = content.path("parts");
                if (parts.isMissingNode() || parts.isEmpty()) continue;
                for (JsonNode part : parts) {
                    if (part.hasNonNull("text")) {
                        String t = part.get("text").asText();
                        if (StringUtils.hasText(t)) {
                            if (!sb.isEmpty()) sb.append('\n');
                            sb.append(t);
                        }
                    }
                }
                if (!sb.isEmpty()) break; // 첫 candidate에서 텍스트 얻으면 종료
            }

            if (sb.isEmpty()) {
                log.warn("[관리자 도서] Gemini 응답에 text parts가 없습니다. Response: {}", responseBody);
                return null;
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("[관리자 도서] Gemini RAG 호출 Error", e);
            return null;
        }
    }
}
