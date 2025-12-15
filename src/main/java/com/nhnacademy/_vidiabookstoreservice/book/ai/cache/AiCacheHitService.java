package com.nhnacademy._vidiabookstoreservice.book.ai.cache;

import com.nhnacademy._vidiabookstoreservice.book.ai.core.VectorUtils;
import com.nhnacademy._vidiabookstoreservice.book.redis.dto.AiCacheEntry;
import com.nhnacademy._vidiabookstoreservice.book.redis.repository.AiSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiCacheHitService {

    private final AiSearchRepository repository;

    private static final int CANDIDATE_LIMIT = 500;
    private static final double SIM_THRESHOLD = 0.88;

    public String tryHit(float[] queryVec) {
        List<String> idList = repository.getRecentEntryIdList(CANDIDATE_LIMIT);
        if (idList.isEmpty()) return null;

        List<AiCacheEntry> entryList = repository.getEntryList(idList);

        double best = -1.0;
        AiCacheEntry bestEntry = null;

        for (AiCacheEntry e : entryList) {
            float[] v = VectorUtils.fromBase64ToFloat(e.vecBase64());
            double sim = VectorUtils.cosine(queryVec, v);
            if (sim > best) {
                best = sim;
                bestEntry = e;
            }
        }

        if (bestEntry != null && best >= SIM_THRESHOLD) {
            return bestEntry.answerJson();
        }
        return null;
    }
}
