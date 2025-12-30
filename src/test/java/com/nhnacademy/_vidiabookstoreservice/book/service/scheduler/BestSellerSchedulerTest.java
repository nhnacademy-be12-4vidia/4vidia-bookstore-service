package com.nhnacademy._vidiabookstoreservice.book.service.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BestSellerSchedulerTest {

    @InjectMocks
    private BestSellerScheduler bestSellerScheduler;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ListOperations<String, String> listOperations;

    private static final String KEY_DAILY_SALES_STATS = "stats:bestseller:daily";
    private static final String KEY_BESTSELLER_VIEW_CACHE = "view:bestseller:top10";
    private static final String KEY_YESTERDAY_BACKUP = "backup:bestseller:yesterday";

    // 오늘 데이터가 충분함 (10개 이상)
    @Test
    @DisplayName("랭킹 갱신 - 오늘 데이터가 충분한 경우 (10개 이상)")
    void updateBestsellerRanking_EnoughTodayData() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(stringRedisTemplate.opsForList()).thenReturn(listOperations);

        Set<TypedTuple<String>> todayTuples = new LinkedHashSet<>();
        for (int i = 1; i <= 10; i++) {
            todayTuples.add(new DefaultTypedTuple<>("Book_" + i, 100.0));
        }
        when(zSetOperations.reverseRangeWithScores(KEY_DAILY_SALES_STATS, 0, 9))
                .thenReturn(todayTuples);

        bestSellerScheduler.updateBestsellerRanking();

        verify(zSetOperations, never()).reverseRangeWithScores(eq(KEY_YESTERDAY_BACKUP), anyLong(), anyLong());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> listCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(listOperations).rightPushAll(anyString(), listCaptor.capture());

        assertThat(listCaptor.getValue()).hasSize(10);

        verify(stringRedisTemplate).rename(anyString(), eq(KEY_BESTSELLER_VIEW_CACHE));
        verify(stringRedisTemplate).persist(KEY_BESTSELLER_VIEW_CACHE);
    }

    // 오늘 데이터 부족 (5개) -> 백업 로직 진입
    // 오늘 데이터 중 score가 null인 경우 -> null 체크 분기 테스트
    // 백업 데이터 중 중복 데이터 존재 -> 중복 스킵 분기 테스트
    // 백업 데이터 중 score가 null인 경우
    // 백업 데이터로 10개를 채워서 루프 break 하는 분기 테스트
    @Test
    @DisplayName("랭킹 갱신 - 데이터 부족(백업 사용) + 중복 제거 + Null 점수 처리 + 10개 채움")
    void updateBestsellerRanking_ComplexScenario() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(stringRedisTemplate.opsForList()).thenReturn(listOperations);

        // 오늘 데이터: 5개 (하나는 점수 Null)
        Set<TypedTuple<String>> todayTuples = new LinkedHashSet<>();
        todayTuples.add(new DefaultTypedTuple<>("Today_1", 10.0));
        todayTuples.add(new DefaultTypedTuple<>("Today_2", null));
        todayTuples.add(new DefaultTypedTuple<>("Duplicate_Book", 5.0)); // 중복 테스트용
        todayTuples.add(new DefaultTypedTuple<>("Today_3", 5.0));
        todayTuples.add(new DefaultTypedTuple<>("Today_4", 5.0));

        when(zSetOperations.reverseRangeWithScores(KEY_DAILY_SALES_STATS, 0, 9))
                .thenReturn(todayTuples);

        // 백업 데이터: 충분히 많이 준비 (중복 포함)
        Set<TypedTuple<String>> backupTuples = new LinkedHashSet<>();
        backupTuples.add(new DefaultTypedTuple<>("Duplicate_Book", 100.0)); // 이미 존재 -> 스킵되어야 함
        backupTuples.add(new DefaultTypedTuple<>("Backup_1", null));
        for (int i = 2; i <= 10; i++) {
            backupTuples.add(new DefaultTypedTuple<>("Backup_" + i, 1.0));
        }

        when(zSetOperations.reverseRangeWithScores(KEY_YESTERDAY_BACKUP, 0, 19))
                .thenReturn(backupTuples);

        bestSellerScheduler.updateBestsellerRanking();

        verify(zSetOperations).reverseRangeWithScores(KEY_YESTERDAY_BACKUP, 0, 19);

        // 저장된 리스트 캡처
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> listCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(listOperations).rightPushAll(anyString(), listCaptor.capture());

        Collection<String> savedList = listCaptor.getValue();

        assertThat(savedList).hasSize(10);
        assertThat(savedList).contains("Today_1", "Today_2", "Duplicate_Book", "Backup_1");
        assertThat(savedList.stream().filter(s -> s.equals("Duplicate_Book")).count()).isEqualTo(1);
    }

    // Redis 조회 결과가 null이거나 데이터가 아예 없는 경우
    @Test
    @DisplayName("랭킹 갱신 - Redis 데이터가 Null이거나 비어있어 갱신하지 않음")
    void updateBestsellerRanking_AllNull_NoUpdate() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(stringRedisTemplate.opsForList()).thenReturn(listOperations);

        // 오늘 데이터 null 리턴
        when(zSetOperations.reverseRangeWithScores(KEY_DAILY_SALES_STATS, 0, 9))
                .thenReturn(null);

        // 백업 데이터 null 리턴
        when(zSetOperations.reverseRangeWithScores(KEY_YESTERDAY_BACKUP, 0, 19))
                .thenReturn(null);

        bestSellerScheduler.updateBestsellerRanking();

        verify(listOperations, never()).rightPushAll(anyString(), anyList());
        verify(stringRedisTemplate, never()).rename(anyString(), anyString());
    }

    // 일일 초기화 - 데이터가 존재하는 경우
    @Test
    @DisplayName("일일 초기화 - 데이터가 있는 경우 (Merge & Delete 수행)")
    void dailyReset_HasData() {
        when(stringRedisTemplate.hasKey(KEY_DAILY_SALES_STATS)).thenReturn(true);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);

        bestSellerScheduler.dailyReset();

        verify(zSetOperations).unionAndStore(KEY_YESTERDAY_BACKUP, KEY_DAILY_SALES_STATS, KEY_YESTERDAY_BACKUP);
        verify(stringRedisTemplate).delete(KEY_DAILY_SALES_STATS);
        verify(stringRedisTemplate).expire(KEY_YESTERDAY_BACKUP, 7, TimeUnit.DAYS);
    }

    // 일일 초기화 - 데이터가 없는 경우
    @Test
    @DisplayName("일일 초기화 - 데이터가 없는 경우 (아무 작업 안 함)")
    void dailyReset_NoData() {
        when(stringRedisTemplate.hasKey(KEY_DAILY_SALES_STATS)).thenReturn(false);

        bestSellerScheduler.dailyReset();

        verify(zSetOperations, never()).unionAndStore(anyString(), anyString(), anyString());
        verify(stringRedisTemplate, never()).delete(anyString());
    }
}