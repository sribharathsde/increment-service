package com.incrementservice.service;

import com.incrementservice.entity.SumEntity;
import com.incrementservice.repository.SumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Handles saving and updating data in Redis to keep the value in the memory and minimize database write frequency
 * On receiving a request for a new key, it stores the key with an expiration time of 10 seconds.
 * If the key is accessed again within this period, only the value is incremented.
 * Additionally, a shadow key is used to track the value; a Redis subscriber shall use this shadow key to fetch the sum and update the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IncrementService {

    private final RedisTemplate<String, Integer> redisTemplate;
    private final SumRepository sumRepository;

    /**
     * Saves or updates a value in Redis.
     *
     * @param key   the key to save or update
     * @param value the value to save or increment
     */
    public void saveOrUpdateDataInRedis(String key, Integer value) {
        String shadowKey = createShadowKey(key);

        // Log the operation
        log.info("Saving/updating data in Redis: key = {}, value = {}", key, value);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key, value);
            redisTemplate.opsForValue().increment(shadowKey, value);
            log.info("Incremented existing key: {}, shadowKey: {}", key, shadowKey);
        } else {
            //Holds the value in Redis and sets 10 seconds for the first time
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(10));
            redisTemplate.opsForValue().set(shadowKey, value);
            log.info("Set new key with expiration: key = {}, shadowKey: {}, duration = 10 seconds", key, shadowKey);
        }
    }

    /**
     * Processes an expired Redis key.
     *
     * @param expiredKey the expired key
     */
    public void processExpiredKey(String expiredKey) {
        String shadowKey = createShadowKey(expiredKey);
        Integer value = redisTemplate.opsForValue().get(shadowKey);
        if (value != null) {
            Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(shadowKey, value, Duration.ofSeconds(10));
            if (Boolean.TRUE.equals(isLocked)) {
                try {
                    log.info("Found value in shadow key: {}, value = {}", shadowKey, value);
                    updateOrInsertRecord(expiredKey, value);
                    redisTemplate.delete(shadowKey);
                    log.info("Deleted shadow key: {}", shadowKey);
                } finally {
                    redisTemplate.delete(shadowKey); // Release the lock after processing
                }
            }
        }
    }

    /**
     * Creates a shadow key name.
     *
     * @param key the original key
     * @return the shadow key name
     */
    private String createShadowKey(String key) {
        String shadowKey = "shadow:" + key;
        log.debug("Created shadow key: {}", shadowKey);
        return shadowKey;
    }

    /**
     * Updates or inserts a record in the database.
     *
     * @param expiredKey the expired Redis key
     * @param value      the value to add to the database
     */
    void updateOrInsertRecord(String expiredKey, Integer value) {
        log.info("Updating or inserting record for key: {}, value = {}", expiredKey, value);

        Optional<SumEntity> existingRecordOpt = sumRepository.findByKey(expiredKey);

        if (existingRecordOpt.isPresent()) {
            SumEntity existingRecord = existingRecordOpt.get();
            existingRecord.setSum(existingRecord.getSum() + value);
            sumRepository.save(existingRecord);
            log.info("Updated existing record: key = {}, new sum = {}", expiredKey, existingRecord.getSum());
        } else {
            SumEntity newRecord = SumEntity.builder().key(expiredKey).sum(value).build();
            sumRepository.save(newRecord);
            log.info("Inserted new record: key = {}, sum = {}", expiredKey, value);
        }
    }
}
