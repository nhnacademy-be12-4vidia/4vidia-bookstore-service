package com.nhnacademy._vidiabookstoreservice.book.ai.rerank;

import com.nhnacademy._vidiabookstoreservice.book.client.RerankerFeignClient;
import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookDocumentRerankerClient implements BookDocumentReranker{

    private static final int DESCRIPTION_MAX_LENGTH = 500;

    private final RerankerFeignClient rerankerClient;

    @Override
    public List<BookDocument> rerankSafely(String query, List<BookDocument> docs) {
        if (docs.isEmpty()) return docs;

        try {
            List<String> documentTextList = docs.stream().map(this::buildRerankText).toList();

            var rerankRequest = new RerankerFeignClient.RerankRequest(query, documentTextList);
            List<RerankerFeignClient.RerankResult> results = rerankerClient.rerank(rerankRequest);

            if (results == null || results.isEmpty()) {
                return docs;
            }

            int size = docs.size();
            List<BookDocument> reranked = results.stream()
                .sorted(Comparator.comparingDouble(RerankerFeignClient.RerankResult::score)
                    .reversed())
                .map(res -> {
                    int idx = res.index();
                    if (idx < 0 || idx >= size) {
                        log.warn("Reranker index out of range: index = {} size = {}", idx, size);
                        return null;
                    }
                    return docs.get(idx);
                })
                .filter(Objects::nonNull)
                .toList();

            return reranked.isEmpty() ? docs : reranked;
        } catch (Exception e) {
            log.error("Reranker 호출 실패 (기본 ES 정렬 사용): {}", e.getMessage());
            return docs;
        }
    }

    private String buildRerankText(BookDocument doc) {
        StringBuilder sb = new StringBuilder();

        if (doc.getTitle() != null) {
            sb.append(doc.getTitle()).append(" ");
        }

        String desc = doc.getDescription();
        if (desc != null) {
            if (desc.length() > DESCRIPTION_MAX_LENGTH) {
                desc = desc.substring(0, DESCRIPTION_MAX_LENGTH);
            }
            sb.append(desc);
        }
        return sb.toString();
    }
}
