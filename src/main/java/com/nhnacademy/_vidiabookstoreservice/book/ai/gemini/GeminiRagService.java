package com.nhnacademy._vidiabookstoreservice.book.ai.gemini;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.aladin.client.AladinApiClient;
import com.nhnacademy._vidiabookstoreservice.book.aladin.dto.AladinItemDto;
import com.nhnacademy._vidiabookstoreservice.book.config.GeminiProperties;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.response.AuthorNameRoleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiRagService {

    @Qualifier("aladinRestClient")
    private final RestClient aladinRestClient;
    private final AladinApiClient aladinApiClient;
    private final RestClient geminiRagRestClient;
    private final ObjectMapper objectMapper;
    private final GeminiProperties geminiProperties;

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

        // 4. 결과 매핑
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
        sb.append("2. 특히 **'목차(bookIndex)'**와 **'상세 설명(description)'**은 반드시 검색을 통해 풍부하게 작성해야 합니다.\n");
        sb.append("3. 저자 정보는 '이름'과 '역할(지은이/옮긴이/그림 등)'을 정확히 구분하여 리스트로 만드세요\n");
        sb.append("3-1. 여러명의 저자를 모두 포함하고, 역할이 불분명한 경우 역할은 비워두세요.\n");
        sb.append("3-2. 저자의 역할이 2개 이상인 경우, 정규화하여 (이름, 역할1), (이름, 역할2) 형태로 각각 추가하세요.\n");
        sb.append("4. categoryCode는 KDC 코드 세자리를 사용하고, 소수점은 사용하지 않습니다. 예: '005', '813'\n");
        sb.append("4-1. 해당 도서에 대한 kdc 정보를 찾지 못한 경우 'UNC'로 기재하세요.\n");
        sb.append("5. 언어는 해당 도서가 작성된 언어로 영어 2글자 소문자를 사용하세요. 예: 'ko', 'en', 'jp'\n");
        sb.append("6. 응답은 반드시 마크다운 코드 블록 없이 **순수 JSON 객체**로만 답변하세요.\n");

        sb.append("\n[목표 JSON 구조]\n");
        sb.append("{\n");
        sb.append("  \"title\": \"도서 제목\",\n");
        sb.append("  \"subtitle\": \"부제 (없으면 빈문자열)\",\n");
        sb.append("  \"authors\": [ {\"name\": \"저자명\", \"role\": \"역할\"} ],\n");
        sb.append("  \"publisher\": \"출판사명\",\n");
        sb.append("  \"publishedDate\": \"YYYY-MM-DD\",\n");
        sb.append("  \"language\": \"언어(ko, en...)\",\n");
        sb.append("  \"pageCount\": 0,\n");
        sb.append("  \"categoryCode\": \"카테고리 코드\",\n");
        sb.append("  \"priceStandard\": 0,\n");
        sb.append("  \"description\": \"상세 설명 (최대한 풍부하게)\",\n");
        sb.append("  \"bookIndex\": \"목차 (줄바꿈 포함)\",\n");
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
            return dbData != null ? dbData : createEmptyResponse();
        }

        try {
            String cleanJson = json.replace("```json", "").replace("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);

            // Gemini 데이터 추출
            List<AuthorNameRoleResponse> authors = new ArrayList<>();
            if (root.has("authors")) {
                authors = objectMapper.convertValue(root.get("authors"), new TypeReference<>() {});
            }

            List<String> tags = new ArrayList<>();
            if (root.has("tags")) {
                try {
                    List<String> geminiTags = objectMapper.convertValue(root.get("tags"), new TypeReference<>() {});
                    tags.addAll(geminiTags);
                } catch (Exception ignored) {}
            }
            if (aladin != null && StringUtils.hasText(aladin.categoryName())) {
                // 중복 제거하며 추가
                for (String cat : aladin.categoryName().split(" > ")) {
                    if (!tags.contains(cat)) tags.add(cat);
                }
            }

            LocalDate publishedDate = null;
            try {
                String dateStr = root.path("publishedDate").asText();
                if (StringUtils.hasText(dateStr)) {
                    // YYYY.MM.DD 형식 대응 및 길이 체크
                    dateStr = dateStr.replace(".", "-");
                    if (dateStr.length() >= 10) {
                        publishedDate = LocalDate.parse(dateStr.substring(0, 10));
                    }
                }
            } catch (Exception ignored) {
                log.warn("[관리자 도서] gemini 날짜 파싱 실패: {}", root.path("publishedDate").asText());
            }

            // 데이터 병합 및 Override (재고와 커버이미지 보호)
            Integer stock = (dbData != null) ? dbData.stock() : 0;
            String coverUrl = (aladin != null) ? aladin.cover() : (dbData != null ? dbData.coverImageUrl() : null);

            return new AdminIsbnSearchResponse(
                    true,
                    coverUrl,
                    root.path("title").asText(dbData != null ? dbData.title() : "제목 없음"),
                    root.path("subtitle").asText(dbData != null ? dbData.subtitle() : null),
                    authors.isEmpty() && dbData != null ? dbData.authors() : authors,
                    root.path("publisher").asText(dbData != null ? dbData.publisher() : null),
                    publishedDate != null ? publishedDate : (dbData != null ? dbData.publishedDate() : null),
                    root.path("language").asText(dbData != null ? dbData.language() : null),
                    root.path("pageCount").asInt(dbData != null ? dbData.pageCount() : 0),
                    root.path("categoryCode").asText(dbData != null ? dbData.categoryCode() : "UNC"),
                    root.path("priceStandard").asInt(dbData != null ? dbData.priceStandard() : 0),
                    stock,
                    root.path("description").asText(dbData != null ? dbData.description() : ""),
                    root.path("bookIndex").asText(dbData != null ? dbData.bookIndex() : ""),
                    tags.isEmpty() && dbData != null ? dbData.tags() : tags
            );

        } catch (Exception e) {
            log.warn("[관리자 도서] gemini 파싱 실패", e);
            return dbData != null ? dbData : createEmptyResponse();
        }
    }

    private AdminIsbnSearchResponse createEmptyResponse() {
        return new AdminIsbnSearchResponse(false, null, "검색 결과 없음", null, List.of(), null, null, null, 0, null, 0, 0, null, null, List.of());
    }

    private String callGeminiWithSearchTool(String prompt) {
        try {
            Map<String, Object> request = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
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
                log.warn("Gemini returned no candidates. Response: {}", responseBody);
                return null;
            }
            JsonNode content = candidates.get(0).path("content");
            if (content.isMissingNode()) {
                log.warn("Gemini candidate has no content (possibly blocked). Response: {}", responseBody);
                return null;
            }
            JsonNode parts = content.path("parts");
            if (parts.isMissingNode() || parts.isEmpty()) {
                return null;
            }
            return parts.get(0).path("text").asText();
        } catch (Exception e) {
            log.error("Gemini RAG Call Error", e);
            return null;
        }
    }
}
