package com.nhnacademy._vidiabookstoreservice.order.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class BestSellerScheduler {
    private final StringRedisTemplate bestsellerRedisTemplate;

    private static final String SOURCE_KEY = "bestseller"; // 오늘 하루 실시간으로 주문이 들어올 때마다 쌓이는 누적 판매량(ZSet)
    private static final String VIEW_KEY = "top10"; // 프론트엔드에서 조회해가는 결과 리스트(List), 스케줄러가 계산을 끝내고 최종 결과만 여기에 넣어줌
    private static final String BACKUP_KEY = "bestseller:prev"; // 어제 판매 데이터 (오늘 판매량이 아직 적을 때, 랭킹 10위를 채우기 위한 보충 데이터로 사용)

    /**
     * 1시간마다 랭킹을 갱신하는
     * - 1. 오늘 데이터 조회: bestseller 키에서 판매량 상위 10개를 가져옴
     * - 2. 데이터 보정 (Backfill): 만약 오늘 판매된 책이 3권밖에 없다면, 나머지 7권은 bestseller:prev(어제 데이터)에서 상위권 순서대로 가져와 채움
     * - 3. 중복 제거 (LinkedHashSet): 오늘 판매된 책이 어제도 판매되었을 수 있음. LinkedHashSet을 사용해 중복을 제거하면서도 랭킹 순서(오늘 판매 우선)는 유지
     * - 4. 최종 저장: 계산된 10개 리스트를 top10 키에 덮어씀. (기존 키 삭제 후 저장)
     */
//    @Scheduled(cron = "0 0/10 * * * *") // 테스트용 10분마다 스케줄링
    @Scheduled(cron = "0 0 * * * *")
    public void updateBestsellerRanking() {
        log.info("========== [Scheduler] 베스트셀러 랭킹 집계 시작 ==========");

        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();
        ListOperations<String, String> listOps = bestsellerRedisTemplate.opsForList();

        Set<String> finalRankingSet = new LinkedHashSet<>(); // 중복 방지, 순서 보장

        Long sourceSize = zSetOps.zCard(SOURCE_KEY);
        log.info("[Scheduler] 베스트셀러 랭킹 갱신 시작");
        log.info("현재 누적된 책 종류: {}개", sourceSize);


        // 1. 오늘의 Top 10 조회 (점수 높은 순)
        Set<ZSetOperations.TypedTuple<String>> todayTop10 =
                zSetOps.reverseRangeWithScores(SOURCE_KEY, 0, 9);

        // Tuple에서 BookId(Value)만 추출하여 리스트로 변환
        if (todayTop10 != null) {
            for (ZSetOperations.TypedTuple<String> tuple : todayTop10) {
                finalRankingSet.add(tuple.getValue());
            }
        }
        log.info(">> 오늘 판매된 도서: {}권 집계됨", finalRankingSet.size());

        // 2. 데이터가 10개 미만일 때 어제 데이터(백업)에서 채우기 로직
        if (finalRankingSet.size() < 10) {
            log.info(">> 데이터 부족 (현재 {}개). 백업 데이터에서 {}개 추가 시도...", finalRankingSet.size(), 10 - finalRankingSet.size());

            Set<String> backupIds = zSetOps.reverseRange(BACKUP_KEY, 0, 19);
            if (backupIds != null) {
                for (String id : backupIds) {
                    if (finalRankingSet.size() >= 10) {
                        break;
                    }
                    finalRankingSet.add(id);
                }
            }
        }

        List<String> finalRankingList = new ArrayList<>(finalRankingSet); // set -> list
        // 3. 결과 저장
        if (!finalRankingList.isEmpty()) {
            bestsellerRedisTemplate.delete(VIEW_KEY); // 기존 랭킹 삭제
            listOps.rightPushAll(VIEW_KEY, finalRankingList);

            log.info(">> 최종 랭킹 반영 완료 (총 {}권)", finalRankingList.size());
            log.info(">> 리스트: {}", finalRankingList);
        } else {
            log.warn(">> [주의] 판매 데이터가 0건입니다. (오늘 + 어제 데이터 없음)");
        }

        log.info("========================================================");
    }

    /**
     * 매일 자정에 데이터를 초기화하는 로직 (하루가 지나면 오늘의 판매량을 어제로 넘기고, 오늘은 0부터 다시 시작해야 함)
     */
//    @Scheduled(cron = "0 0/30 * * * *") // 테스트용 매 시간마다 초기화
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyReset() {
        log.info("========== [Scheduler] 일일 데이터 초기화 및 백업 수행 ==========");

        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();
        ListOperations<String, String> listOps = bestsellerRedisTemplate.opsForList();

        // 1. 어제 랭킹(top10) 백업
        List<String> yesterdayTop10 = listOps.range(VIEW_KEY, 0, 9);

        if (yesterdayTop10 != null && !yesterdayTop10.isEmpty()) {
            bestsellerRedisTemplate.delete(BACKUP_KEY);

            for (String bookId : yesterdayTop10) {
                zSetOps.add(BACKUP_KEY, bookId, 0);
            }

            log.info(">> 어제 랭킹 {}권 백업 완료", yesterdayTop10.size());
        } else {
            log.warn(">> 어제 랭킹 데이터 없음");
        }

        // 2. 오늘 집계 초기화
        bestsellerRedisTemplate.delete(SOURCE_KEY);
        log.info(">> 오늘 판매 집계 초기화 완료");

        log.info("=============================================================");
    }
}
