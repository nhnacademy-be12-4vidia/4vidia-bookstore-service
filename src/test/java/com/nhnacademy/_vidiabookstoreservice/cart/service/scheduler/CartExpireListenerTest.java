package com.nhnacademy._vidiabookstoreservice.cart.service.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CartExpireListenerTest {

    private CartExpireListener cartExpireListener;
    private static final String CART_DB_CHANNEL = "__keyevent@384__:expired";
    private static final String EXPIRE_KEY = "cart:expire:user:123";

    @BeforeEach
    void setUp() {
        cartExpireListener = new CartExpireListener();
    }

    @Test
    @DisplayName("onMessage: 올바른 채널과 키가 들어오면 userId를 추출하여 저장한다")
    void onMessage_Success() {
        // given
        Message message = new DefaultMessage(
                CART_DB_CHANNEL.getBytes(),
                EXPIRE_KEY.getBytes()
        );

        // when
        cartExpireListener.onMessage(message, null);

        // then
        Set<Long> expiredUserIds = cartExpireListener.consumeExpiredUserIds();
        assertThat(expiredUserIds).containsOnly(123L);
    }

    @Test
    @DisplayName("onMessage: 지정된 Redis DB(@384)가 아닌 채널 메시지는 무시한다")
    void onMessage_IgnoreWrongDb() {
        // given (DB 번호가 다른 경우)
        Message message = new DefaultMessage(
                "__keyevent@0__:expired".getBytes(),
                EXPIRE_KEY.getBytes()
        );

        // when
        cartExpireListener.onMessage(message, null);

        // then
        assertThat(cartExpireListener.consumeExpiredUserIds()).isEmpty();
    }

    @Test
    @DisplayName("onMessage: 장바구니 만료 키가 아닌 다른 키의 만료는 무시한다")
    void onMessage_IgnoreOtherPrefix() {
        // given
        Message message = new DefaultMessage(
                CART_DB_CHANNEL.getBytes(),
                "other:key:123".getBytes()
        );

        // when
        cartExpireListener.onMessage(message, null);

        // then
        assertThat(cartExpireListener.consumeExpiredUserIds()).isEmpty();
    }

    @Test
    @DisplayName("consumeExpiredUserIds: 호출 시 데이터를 반환하고 내부 저장소는 비워야 한다")
    void consumeExpiredUserIds_ClearsAfterReturn() {
        // given
        Message message = new DefaultMessage(CART_DB_CHANNEL.getBytes(), EXPIRE_KEY.getBytes());
        cartExpireListener.onMessage(message, null);

        // when
        Set<Long> firstCall = cartExpireListener.consumeExpiredUserIds();
        Set<Long> secondCall = cartExpireListener.consumeExpiredUserIds();

        // then
        assertThat(firstCall).containsOnly(123L);
        assertThat(secondCall).isEmpty(); // 두 번째 호출은 비어있어야 함
    }
}