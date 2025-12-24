package com.nhnacademy._vidiabookstoreservice.book.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.book.ai.AiWarmupService;
import com.nhnacademy._vidiabookstoreservice.book.ai.cache.AiCacheHitService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiAnswerService;
import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BaseBookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookSearchListResponse;

import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiBookSuggestion;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchWithTagRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.AiBookSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.ai.embedding.EmbeddingService;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.AiCacheResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.SearchBooksResponse;
import com.nhnacademy._vidiabookstoreservice.book.redis.repository.AiSearchRepository;
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

    private static final int MAX_RESULTS = 500;
    private static final int TOP_SCORE_RESULTS = 100;
    private static final int TAG_MAX_RESULTS = 10000;

    private final EmbeddingService embeddingService;
    private final BookDocumentSearchClient searchClient;
    private final BookDocumentReranker reranker;
    private final BookSearchResultAssembler resultAssembler;
    private final GeminiAnswerService geminiAnswerService;
    private final AiWarmupService aiWarmupService;
    private final AiCacheHitService aiCacheHitService;
    private final ObjectMapper objectMapper;
    private final AiSearchRepository aiSearchRepository;

    public SearchBooksResponse searchBooks(EsBookSearchRequest request, Pageable pageable, Long userId) {
        String rawKeyword = request.getKeyword();
        String keyword = (rawKeyword == null) ? "" : rawKeyword.trim().toLowerCase();
        List<GeminiBookSuggestion> suggestionList = null;
        if (!StringUtils.hasText(keyword)) {
            return new SearchBooksResponse(PageResponse.from(Page.empty(pageable)), null);
        }

        List<BookDocument> initialDocs = searchClient.search(request, null, MAX_RESULTS);
        List<BookDocument> topScoreDocs = initialDocs.stream().limit(TOP_SCORE_RESULTS).toList();

        if (initialDocs.isEmpty()) {
            return new SearchBooksResponse(PageResponse.from(Page.empty(pageable)), null);
        }
        float[] queryVector = embeddingService.embedOrNull(keyword);
        boolean hit = false;
        String hitEntryId = "";
        if (queryVector != null && queryVector.length > 0) {
            hitEntryId = aiCacheHitService.tryHit(queryVector);
            hit = (StringUtils.hasText(hitEntryId));
            if (hit) {
                log.info("[SEARCH] skip warmup(cache hit) keyword = {}", keyword);
                String rawSuggestion = aiSearchRepository.getAnswerJson(hitEntryId);
                try {
                    suggestionList = objectMapper.readValue(rawSuggestion, new TypeReference<List<GeminiBookSuggestion>>() {
                    });
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                log.warn("[AI-WARMUP-CALL] kw='{}' thread={} userId={}",
                        keyword, Thread.currentThread().getName(), userId);
                aiWarmupService.warmUpAndCache(keyword, topScoreDocs);
            }
        }
        Page<BaseBookListResponse> pageResponse = resultAssembler.assemble(initialDocs, userId, pageable, true);

        if (suggestionList != null && !suggestionList.isEmpty()) {
            List<AiCacheResponse> aiCacheList = resultAssembler.assembleCache(suggestionList);
            return new SearchBooksResponse(PageResponse.from(pageResponse), aiCacheList);
        }
        return new SearchBooksResponse(PageResponse.from(pageResponse), null);
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

        List<BookDocument> initialDocs = searchClient.search(request, queryVector, TOP_SCORE_RESULTS);

        if (initialDocs.isEmpty()) {
            return AiBookSearchResponse.builder()
                .results(PageResponse.from(Page.empty(pageable)))
                .aiAnswer("검색 결과가 없습니다.")
                .build();
        }

        List<BookDocument> rerankDocs = reranker.rerankSafely(keywordNormal, initialDocs);

        Page<BaseBookListResponse> pageResult = resultAssembler.assemble(rerankDocs, userId, pageable, true);

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
            .collect(Collectors.toMap(BaseBookListResponse::getId, dto -> (BookSearchListResponse) dto));


        List<BaseBookListResponse> rankedDtos = (List<BaseBookListResponse>) suggestionList.stream()
            .sorted(Comparator.comparingInt(GeminiBookSuggestion::getRank))
            .map(s -> {
                BaseBookListResponse base = dtoMap.get(s.getBookId());
                if (base == null) {
                    return null;
                }
                return ((BookSearchListResponse) base).toBuilder()
                    .rank(s.getRank())
                    .relevanceScore(s.getRelevanceScore())
                    .recommended(s.isRecommended())
                    .llmSummary(s.getSummary())
                    .build();
            })
            .filter(dto -> dto != null)
            .toList();

        Page<BaseBookListResponse> enrichedPage =
            new PageImpl<>(rankedDtos, pageable, rankedDtos.size());

        PageResponse<BaseBookListResponse> pageResponse =
            PageResponse.from(enrichedPage);

        String aiAnswer = ""; // 필요하면 나중에 LLM natural answer 넣기

        return AiBookSearchResponse.builder()
            .results(pageResponse)
            .aiAnswer(aiAnswer)
            .build();
    }

    public PageResponse<BaseBookListResponse> searchBooksByTags(EsBookSearchWithTagRequest request, Pageable pageable, Long userId) {
        List<BookDocument> docs = searchClient.searchByTag(request, TAG_MAX_RESULTS);

        if (docs == null || docs.isEmpty()) {
            return PageResponse.from(Page.empty(pageable));
        }

        Page<BaseBookListResponse> page = resultAssembler.assemble(docs, userId, pageable, false);
        return PageResponse.from(page);
    }

    public PageResponse<BaseBookListResponse> searchBooksByTagOrderByRating(String tagName, boolean asc, Pageable pageable, Long userId) {
        List<BookDocument> docs = searchClient.searchByTagOrderByRating(tagName, asc, TAG_MAX_RESULTS);

        if (docs == null || docs.isEmpty()) {
            return PageResponse.from(Page.empty(pageable));
        }

        Page<BaseBookListResponse> page = resultAssembler.assemble(docs, userId, pageable, false);

        return PageResponse.from(page);

    }

    public List<BookListResponse> searchBooksForCoupon(String bookTitle) {
        EsBookSearchRequest request = EsBookSearchRequest.builder().keyword(bookTitle).build();
        List<BookDocument> docs = searchClient.search(request, null, 50);

        List<Long> idList = docs.stream().map(BookDocument::getId).map(Long::parseLong).toList();


    }


}