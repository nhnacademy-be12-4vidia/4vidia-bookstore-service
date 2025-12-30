package com.nhnacademy._vidiabookstoreservice.book.redis.repository;

import com.nhnacademy._vidiabookstoreservice.book.redis.dto.AiCacheEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AiSearchRepository {

    @Qualifier("aiRedisTemplate")
    private final StringRedisTemplate aiRedisTemplate;

    private static final String ENTRY_PREFIX = "ai:entry:";
    private static final String RECENT_KEY = "ai:recent";
    private static final String LOCK_PREFIX = "ai:lock:";

    public boolean tryLock(String lockKey, Duration ttl) {
        Boolean ok = aiRedisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + lockKey, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }

    public void saveEntry(AiCacheEntry e, Duration ttl, int recentLimit) {
        String key = ENTRY_PREFIX + e.entryId();

        Map<String, String> map = new HashMap<>();
        map.put("kw", e.keyword());
        map.put("answer", e.answerJson());
        map.put("vec", e.vecBase64());
        map.put("createdAt", String.valueOf(e.createdAtMs()));

        aiRedisTemplate.opsForHash().putAll(key, map);
        aiRedisTemplate.expire(key, ttl);

        aiRedisTemplate.opsForList().leftPush(RECENT_KEY, e.entryId());
        aiRedisTemplate.opsForList().trim(RECENT_KEY, 0, recentLimit - 1L);
    }

    public List<String> getRecentEntryIdList(int limit) {
        List<String> idList = aiRedisTemplate.opsForList().range(RECENT_KEY, 0, limit - 1L);
        return idList == null ? List.of() : idList;
    }

    public List<AiCacheEntry> getEntryList(List<String> entryIdList) {
        if (entryIdList.isEmpty()) return List.of();

        List<AiCacheEntry> out = new ArrayList<>(entryIdList.size());
        for (String id : entryIdList) {
            String key = ENTRY_PREFIX + id;
            Map<Object, Object> m = aiRedisTemplate.opsForHash().entries(key);
            if (m.isEmpty()) continue;

            String kw = (String) m.get("kw");
            String answer = (String) m.get("answer");
            String vec = (String) m.get("vec");
            String createdAt = (String) m.get("createdAt");
            if (answer == null || vec == null) continue;

            long createdAtMs = 0L;
            try {
                if (createdAt != null) {
                    createdAtMs = Long.parseLong(createdAt);
                }
            }catch (Exception ignore) {}

            out.add(new AiCacheEntry(id, kw, answer, vec, createdAtMs));
        }
        return out;
    }

    public AiCacheEntry getEntry(String entryId) {
        String key = ENTRY_PREFIX + entryId;

        Map<Object, Object> m = aiRedisTemplate.opsForHash().entries(key);
        if (m.isEmpty()) return null;

        String kw = (String) m.get("kw");
        String answer = (String) m.get("answer");
        String vec = (String) m.get("vec");
        String createdAt = (String) m.get("createdAt");

        return new AiCacheEntry(key, kw, answer, vec, Long.parseLong(createdAt));
    }

    public String getAnswerJson(String entryId) {
        String key = ENTRY_PREFIX + entryId;

        Map<Object, Object> m = aiRedisTemplate.opsForHash().entries(key);
        if (m.isEmpty()) return null;

        return (String) m.get("answer");
    }
}
