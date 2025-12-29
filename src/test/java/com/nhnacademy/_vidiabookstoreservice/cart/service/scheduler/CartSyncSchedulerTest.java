package com.nhnacademy._vidiabookstoreservice.cart.service.scheduler;

import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartSyncSchedulerTest {

    @InjectMocks
    private CartSyncScheduler cartSyncScheduler;

    @Mock
    private CartExpireListener cartExpireListener;

    @Mock
    private CartService cartService;

    @Test
    @DisplayName("flushExpiredCarts: 만료된 유저 ID가 있으면 각각 flush 서비스를 호출한다")
    void flushExpiredCarts_Success() {
        // given
        Set<Long> expiredIds = Set.of(1L, 2L);
        when(cartExpireListener.consumeExpiredUserIds()).thenReturn(expiredIds);

        // when
        cartSyncScheduler.flushExpiredCarts();

        // then
        verify(cartService, times(1)).flushCartFromRedisToMySql(1L);
        verify(cartService, times(1)).flushCartFromRedisToMySql(2L);
    }

    @Test
    @DisplayName("flushExpiredCarts: 만료된 ID가 없으면 서비스를 호출하지 않는다")
    void flushExpiredCarts_Empty_NoCall() {
        // given
        when(cartExpireListener.consumeExpiredUserIds()).thenReturn(Collections.emptySet());

        // when
        cartSyncScheduler.flushExpiredCarts();

        // then
        verify(cartService, never()).flushCartFromRedisToMySql(anyLong());
    }

    @Test
    @DisplayName("flushExpiredCarts: 특정 유저 flush 중 예외가 발생해도 다른 유저 처리는 계속되어야 한다")
    void flushExpiredCarts_Exception_Isolated() {
        // given
        Set<Long> expiredIds = Set.of(1L, 2L);
        when(cartExpireListener.consumeExpiredUserIds()).thenReturn(expiredIds);

        // 1번 유저는 에러 발생, 2번 유저는 정상 처리 가정
        doThrow(new RuntimeException("DB Error")).when(cartService).flushCartFromRedisToMySql(1L);

        // when
        cartSyncScheduler.flushExpiredCarts();

        // then
        // 에러가 났더라도 2번 유저의 flush가 호출되었는지 확인
        verify(cartService).flushCartFromRedisToMySql(1L);
        verify(cartService).flushCartFromRedisToMySql(2L);
    }
}