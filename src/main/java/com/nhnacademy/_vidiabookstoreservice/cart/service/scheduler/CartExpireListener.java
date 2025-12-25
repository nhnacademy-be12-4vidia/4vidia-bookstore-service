package com.nhnacademy._vidiabookstoreservice.cart.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CartExpireListener implements MessageListener {

    private static final String CART_EXPIRE_PREFIX = "cart:expire:user:";
    private static final String CART_DB = "@384"; // cart Redis DB

    private final Set<Long> expiredUserIds = ConcurrentHashMap.newKeySet();

    @Override
    public void onMessage(Message message, byte[] pattern) {

        String channel = new String(message.getChannel());

        String expiredKey = new String(message.getBody());

        if (!channel.contains(CART_DB)) {
            return;
        }

        if (!expiredKey.startsWith(CART_EXPIRE_PREFIX)) {
            return;
        }

        Long userId = Long.parseLong(
                expiredKey.substring(CART_EXPIRE_PREFIX.length())
        );

        log.info("🔥 장바구니 만료 이벤트 감지 userId={}", userId);
        expiredUserIds.add(userId);
    }

    public Set<Long> consumeExpiredUserIds() {
        Set<Long> result = Set.copyOf(expiredUserIds);
        expiredUserIds.clear();
        return result;
    }
}
