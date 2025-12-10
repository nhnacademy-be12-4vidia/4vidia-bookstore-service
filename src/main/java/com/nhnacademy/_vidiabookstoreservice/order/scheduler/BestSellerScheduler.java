package com.nhnacademy._vidiabookstoreservice.order.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class BestSellerScheduler {

    private final StringRedisTemplate bestsellerRedisTemplate;

    /**
     * 매 시간 초기화
     */
    @Scheduled(cron = "0 0 * * * *")
    public void calculateAndReset() {
        log.info("BestSeller Daily calculation started.");

        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();

        // 1. 상위 10개 조회
        Set<ZSetOperations.TypedTuple<String>> top10 =
                zSetOps.reverseRangeWithScores("bestseller", 0, 9);

        // 2. 저장? 일지도?
         if (top10 != null && !top10.isEmpty()) {
             bestsellerRedisTemplate.delete("top10");
             bestsellerRedisTemplate.opsForZSet().add("top10", top10);

             Set<ZSetOperations.TypedTuple<String>> savedTop10 =
                     zSetOps.reverseRangeWithScores("top10", 0, -1);
             if (savedTop10 != null) {
                 String logContent = savedTop10.stream()
                         .map(tuple -> "BookID: " + tuple.getValue() + ", Score: " + tuple.getScore())
                         .collect(Collectors.joining(" | "));
                 log.info("Redis 'top10' content: {}", logContent);
             }

            log.info("Successfully added {} items to 'top10' ranking.", top10.size());
        } else {
            log.warn("No bestseller data found in 'bestseller' key. Skipping 'top10' update.");
        }


        // 3. Redis 초기화
        bestsellerRedisTemplate.delete("bestseller");
    }
}
