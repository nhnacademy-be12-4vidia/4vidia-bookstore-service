package com.nhnacademy._vidiabookstoreservice.book.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final String KEY_DAILY_SALES_STATS = "stats:bestseller:daily"; // 오늘 하루 실시간으로 주문이 들어올 때마다 쌓이는 누적 판매량(ZSet)
    private static final String KEY_BESTSELLER_VIEW_CACHE = "view:bestseller:top10"; // 프론트엔드에서 조회해가는 결과 리스트(List), 스케줄러가 계산을 끝내고 최종 결과만 여기에 넣어줌
    private static final String KEY_YESTERDAY_BACKUP = "backup:bestseller:yesterday"; // 어제 판매 데이터 (오늘 판매량이 아직 적을 때, 랭킹 10위를 채우기 위한 보충 데이터로 사용)

    /**
     * 1시간마다 랭킹을 갱신하는
     * - 1. 오늘 데이터 조회: bestseller 키에서 판매량 상위 10개를 가져옴
     * - 2. 데이터 보정 (Backfill): 만약 오늘 판매된 책이 3권밖에 없다면, 나머지 7권은 backup:bestseller:yesterday(어제 데이터)에서 상위권 순서대로 가져와 채움
     * - 3. 중복 제거 (LinkedHashSet): 오늘 판매된 책이 어제도 판매되었을 수 있음. LinkedHashSet을 사용해 중복을 제거하면서도 랭킹 순서(오늘 판매 우선)는 유지
     * - 4. 최종 저장: 계산된 10개 리스트를 top10 키에 덮어씀. (기존 키 삭제 후 저장)
     */
//    @Scheduled(cron = "0 0/5 * * * *") // 테스트용 10분마다 스케줄링
    @Scheduled(cron = "0 0 * * * *")
    public void updateBestsellerRanking() {
        log.info("========== [Scheduler] 베스트셀러 랭킹 집계 시작 ==========");

        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();
        ListOperations<String, String> listOps = bestsellerRedisTemplate.opsForList();

        Set<String> finalRankingSet = new LinkedHashSet<>(); // 중복 방지, 순서 보장

        Long sourceSize = zSetOps.zCard(KEY_DAILY_SALES_STATS);
        log.info("[Scheduler] 베스트셀러 랭킹 갱신 시작");
        log.info("현재 누적된 책 종류: {}개", sourceSize);


        // 1. 오늘의 Top 10 조회 (점수 높은 순)
        Set<String> todayTop10 = zSetOps.reverseRange(KEY_DAILY_SALES_STATS, 0, 9);

        if (todayTop10 != null) {
            finalRankingSet.addAll(todayTop10);
        }
        log.info(">> 오늘 판매된 도서: {}권 집계됨 (우선 순위)", finalRankingSet.size());


        // 2. 데이터가 10개 미만일 때 어제 데이터(백업)에서 채우기 로직
        if (finalRankingSet.size() < 10) {
            log.info(">> 데이터 부족 (현재 {}개). 백업 데이터에서 상위권 도서 보충 시도...", finalRankingSet.size());

            // 백업 데이터에서 넉넉하게 상위 20개 가져옴 (중복이 있을 수 있으므로)
            Set<String> backupIds = zSetOps.reverseRange(KEY_YESTERDAY_BACKUP, 0, 19);

            if (backupIds != null) {
                for (String id : backupIds) {
                    if (finalRankingSet.size() >= 10) {
                        break; // 10개가 채워지면 즉시 중단
                    }
                    // LinkedHashSet이므로 이미 오늘 판매된 책이라면(중복) 무시되고,
                    // 없으면 리스트의 맨 뒤에 추가됨 -> 순위 밀림 효과 자동 적용
                    finalRankingSet.add(id);
                }
            }
        }

        List<String> finalRankingList = new ArrayList<>(finalRankingSet); // set -> list
        // 3. 결과 저장
        if (!finalRankingList.isEmpty()) {
            bestsellerRedisTemplate.delete(KEY_BESTSELLER_VIEW_CACHE); // 기존 랭킹 삭제
            listOps.rightPushAll(KEY_BESTSELLER_VIEW_CACHE, finalRankingList);

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

        Boolean hasTodayData = bestsellerRedisTemplate.hasKey(KEY_DAILY_SALES_STATS);

        if (Boolean.TRUE.equals(hasTodayData)) {
            // 오늘 집계(ZSet) -> 어제 백업(ZSet)으로 이름 변경 (덮어쓰기)
            bestsellerRedisTemplate.rename(KEY_DAILY_SALES_STATS, KEY_YESTERDAY_BACKUP);
            log.info(">> 오늘 판매 데이터를 백업 키({})로 이관 완료 (점수 보존)", KEY_YESTERDAY_BACKUP);
        } else {
            // 오늘 판매량이 하나도 없었다면, 백업 데이터도 비워야 함 (어제의 어제 데이터가 남지 않도록)
            bestsellerRedisTemplate.delete(KEY_YESTERDAY_BACKUP);
            log.info(">> 오늘 판매 데이터 없음. 백업 데이터 초기화.");
        }

        log.info("=============================================================");
    }
}
