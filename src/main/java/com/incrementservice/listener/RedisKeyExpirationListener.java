package com.incrementservice.listener;

import com.incrementservice.service.IncrementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Handles Redis key expiration events.
 */
@Component
@AllArgsConstructor
@Slf4j
public class RedisKeyExpirationListener implements MessageListener {

    private final IncrementService incrementService;

    /**
     * Processes expired Redis keys.
     *
     * @param message the message containing the expired key
     * @param pattern the subscription pattern (not used)
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {

        String expiredKey = message.toString();

        log.info("Processing expired Redis key: {}", expiredKey);

        incrementService.processExpiredKey(expiredKey);
    }
}