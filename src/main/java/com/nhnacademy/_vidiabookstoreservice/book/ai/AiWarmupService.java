package com.nhnacademy._vidiabookstoreservice.book.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.book.ai.core.VectorUtils;
import com.nhnacademy._vidiabookstoreservice.book.ai.embedding.OllamaEmbeddingService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiAnswerService;
import com.nhnacademy._vidiabookstoreservice.book.ai.rerank.BookDocumentReranker;
import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.dto.gemini.GeminiBookSuggestion;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.AiBookSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.redis.dto.AiCacheEntry;
import com.nhnacademy._vidiabookstoreservice.book.redis.repository.AiSearchRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.search.BookSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiWarmupService {
    private final AiSearchRepository aiSearchRepository;
    private final GeminiAnswerService geminiAnswerService;
    private final OllamaEmbeddingService ollamaEmbeddingService;
    private final ObjectMapper objectMapper;
    private final BookDocumentReranker reranker;

    private static final Duration ENTRY_TTL = Duration.ofHours(6);
    private static final Duration LOCK_TTL = Duration.ofSeconds(60);
    private static final int RECENT_LIMIT = 500;

    @Async("aiExecutor")
    public void warmUpAndCache(String keywordNormal, List<BookDocument> initialDocs) {
        String lockKey = Integer.toHexString(keywordNormal.hashCode());
        if (!aiSearchRepository.tryLock(lockKey, LOCK_TTL)) {
            log.debug("[AI-WARMUP] skip(lock exists) key={}", lockKey);
            return;
        }

        try {
            List<BookDocument> topForLlm = reranker.rerankSafely(keywordNormal, initialDocs).stream().limit(10).toList();
            List<GeminiBookSuggestion> suggestionList = geminiAnswerService.generateSuggestions(keywordNormal, topForLlm);

            String answerJson = objectMapper.writeValueAsString(suggestionList);

            float[] vec = ollamaEmbeddingService.embedOrNull(keywordNormal);
            if (vec == null || vec.length == 0) {
                log.warn("[AI-WARMUP] skip(cache) embedding is null/empty. keyword={}", keywordNormal);
                return;
            }

            String entryId = UUID.randomUUID().toString();
            String vecB64 = VectorUtils.toBase64(vec);

            AiCacheEntry entry = new AiCacheEntry(
                    entryId,
                    keywordNormal,
                    answerJson,
                    vecB64,
                    System.currentTimeMillis()
            );

            aiSearchRepository.saveEntry(entry, ENTRY_TTL, RECENT_LIMIT);
            log.info("[AI-WARMUP] cached entryID = {}", entryId);

        } catch (JsonProcessingException e) {
            log.warn("[AI-WARMUP] json serialize failed. keyword={} msg={}", keywordNormal, e.getMessage(), e);
            return;
        } catch (Exception e) {
            log.warn("[AI-WARMUP] failed. keyword={} msg={}", keywordNormal, e.getMessage(), e);
            return;
        }
    }
}
