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

    @Qualifier("bestsellerRedisTemplate")
    private final StringRedisTemplate bestsellerRedisTemplate;

    private static final String SOURCE_KEY = "bestseller"; // 누적 판매량 (계속 쌓이게)
    private static final String VIEW_KEY = "top10"; // 조회용 캐시 (사용자에게 보여줌)

    /**
     * 매 시간 베스트셀러 랭킹 갱신
     * SOURCE_KEY에서 top10 을 뽑아서 VIEW_KEY로 덮어씁니다
     */
//    @Scheduled(cron = "0 0/5 * * * *") // 테스트용 5분마다 스케줄링
    @Scheduled(cron = "0 0 * * * *")
    public void updateBestsellerRanking() {
        log.info("베스트셀러 랭킹(top10) 집계 시작");

        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();

        // 1. 상위 10개 조회
        Set<ZSetOperations.TypedTuple<String>> top10 =
                zSetOps.reverseRangeWithScores(SOURCE_KEY, 0, 9);

        if (top10 == null || top10.isEmpty()) {
            log.warn("베스트셀러 판매 데이터 없음. 랭킹 갱신 건너 뜀.");
            return;
        }

        // 2. 조회용 키(VIEW_KEY) 갱신 (기존 데이터 지우고 새로 넣기)
        try {
            bestsellerRedisTemplate.delete(VIEW_KEY);
            zSetOps.add(VIEW_KEY, top10);

            String logContent = top10.stream()
                    .map(tuple -> String.format("[%s:%.0f]", tuple.getValue(), tuple.getScore()))
                    .collect(Collectors.joining(", "));

            log.info("베스트셀러 랭킹 갱신 완료 ({}건): {}", top10.size(), logContent);
        } catch (Exception e) {
            log.error("베스트셀러 랭킹 갱신 중 redis 오류 발생", e);
        }

        // 어제 날짜 기준 상위 10개
        // 다음날 10개 가져오는데 가져올때 개수 초기화
        // if 어제 0종류 팔려도
        // 오늘 띄울거 10개 안되면 어제거 채워서 10개
    }
}
