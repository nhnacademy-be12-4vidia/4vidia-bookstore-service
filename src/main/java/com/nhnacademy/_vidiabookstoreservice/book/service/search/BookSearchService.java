package com.nhnacademy._vidiabookstoreservice.book.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.book.ai.AiWarmupService;
import com.nhnacademy._vidiabookstoreservice.book.ai.cache.AiCacheHitService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiAnswerService;
import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookSearchListResponse;

import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiBookSuggestion;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.AiBookSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.ai.embedding.EmbeddingService;
import com.nhnacademy._vidiabookstoreservice.book.service.search.es.BookDocumentSearchClient;
import com.nhnacademy._vidiabookstoreservice.book.ai.rerank.BookDocumentReranker;
import com.nhnacademy._vidiabookstoreservice.book.service.search.result.BookSearchResultAssembler;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookSearchService {

    private static final int MAX_RESULTS = 50;

    private final EmbeddingService embeddingService;
    private final BookDocumentSearchClient searchClient;
    private final BookDocumentReranker reranker;
    private final BookSearchResultAssembler resultAssembler;
    private final GeminiAnswerService geminiAnswerService;
    private final AiWarmupService aiWarmupService;
    private final AiCacheHitService aiCacheHitService;
    private final ObjectMapper objectMapper;

    public Page<BookSearchListResponse> searchBooks(EsBookSearchRequest request, Pageable pageable, Long userId) {
        String rawKeyword = request.getKeyword();
        String keyword = (rawKeyword == null) ? "" : rawKeyword.trim().toLowerCase();
        if (!StringUtils.hasText(keyword)) {
            return Page.empty(pageable);
        }

        List<BookDocument> initialDocs = searchClient.search(request, null, MAX_RESULTS);

        if (initialDocs.isEmpty()) {
            return Page.empty(pageable);
        }
        float[] queryVector = embeddingService.embedOrNull(keyword);
        boolean hit = false;
        if (queryVector != null && queryVector.length > 0) {
            String hitEntryId = aiCacheHitService.tryHit(queryVector);
            hit = (hitEntryId != null);
        }

        if (!hit) {
            log.warn("[AI-WARMUP-CALL] kw='{}' thread={} uri=? userId={}",
                    keyword, Thread.currentThread().getName(), userId,
                    new RuntimeException("caller-trace"));
            aiWarmupService.warmUpAndCache(keyword, initialDocs);
        } else {
            log.info("[SEARCH] skip warmup(cache hit) keyword = {}", keyword);
        }

        return resultAssembler.assemble(initialDocs, userId, pageable);
    }

    public AiBookSearchResponse searchBookWithLlm(EsBookSearchRequest request, Pageable pageable,
        Long userId) {

        String keyword = request.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return AiBookSearchResponse.builder()
                .results(PageResponse.from(Page.empty(pageable)))
                .aiAnswer("")
                .build();
        }

        String keywordNormal = keyword.trim().toLowerCase();

        boolean useSemantic = Boolean.TRUE.equals(request.getUseSemantic());
        float[] queryVector = null;

        if (useSemantic) {
            queryVector = embeddingService.embedOrNull(keywordNormal);
        }

        List<BookDocument> initialDocs = searchClient.search(request, queryVector, MAX_RESULTS);

        if (initialDocs.isEmpty()) {
            return AiBookSearchResponse.builder()
                .results(PageResponse.from(Page.empty(pageable)))
                .aiAnswer("검색 결과가 없습니다.")
                .build();
        }

        List<BookDocument> rerankDocs = reranker.rerankSafely(keywordNormal, initialDocs);

        Page<BookSearchListResponse> pageResult = resultAssembler.assemble(rerankDocs, userId,
            pageable);

        List<BookDocument> topForLlm = rerankDocs.stream()
            .limit(10)
            .toList();


        boolean hit = false;
        String hitEntryId = "";
        List<GeminiBookSuggestion> suggestionList;

        if (queryVector != null && queryVector.length > 0) {
            hitEntryId= aiCacheHitService.tryHit(queryVector);
            hit = (hitEntryId != null);
        }

        if (hit) {
            String hitEntryAnswer = aiCacheHitService.getAnswerJson(hitEntryId);
            try {
                suggestionList = objectMapper.readValue(hitEntryAnswer, new TypeReference<List<GeminiBookSuggestion>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        } else {
            suggestionList = geminiAnswerService.generateSuggestions(keyword,
                    topForLlm);
            aiWarmupService.cacheGeminiAnswer(keywordNormal, suggestionList);
        }

        Map<Long, BookSearchListResponse> dtoMap = pageResult.getContent().stream()
            .collect(Collectors.toMap(BookSearchListResponse::getId, dto -> dto));


        List<BookSearchListResponse> rankedDtos = suggestionList.stream()
            .sorted(Comparator.comparingInt(GeminiBookSuggestion::getRank))
            .map(s -> {
                BookSearchListResponse base = dtoMap.get(s.getBookId());
                if (base == null) {
                    return null;
                }
                return base.toBuilder()
                    .rank(s.getRank())
                    .relevanceScore(s.getRelevanceScore())
                    .recommended(s.isRecommended())
                    .llmSummary(s.getSummary())
                    .build();
            })
            .filter(dto -> dto != null)
            .toList();

        Page<BookSearchListResponse> enrichedPage =
            new PageImpl<>(rankedDtos, pageable, rankedDtos.size());

        PageResponse<BookSearchListResponse> pageResponse =
            PageResponse.from(enrichedPage);

        String aiAnswer = ""; // 필요하면 나중에 LLM natural answer 넣기

        return AiBookSearchResponse.builder()
            .results(pageResponse)
            .aiAnswer(aiAnswer)
            .build();
    }

}