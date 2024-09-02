package com.incrementservice.service;

import com.incrementservice.entity.SumEntity;
import com.incrementservice.repository.SumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IncrementServiceTest {

    @Mock
    private RedisTemplate<String, Integer> redisTemplate;

    @Mock
    private ValueOperations<String, Integer> valueOperations;

    @Mock
    private SumRepository sumRepository;

    @InjectMocks
    private IncrementService incrementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSaveOrUpdateDataInRedis_NewKey() {
        String key = "testKey";
        Integer value = 10;
        String shadowKey = "shadow:" + key;

        when(redisTemplate.hasKey(key)).thenReturn(false);

        incrementService.saveOrUpdateDataInRedis(key, value);

        verify(valueOperations).set(eq(key), eq(value), eq(Duration.ofSeconds(10)));
        verify(valueOperations).set(eq(shadowKey), eq(value));
        verify(valueOperations, never()).increment(anyString(), anyInt());
    }

    @Test
    void testSaveOrUpdateDataInRedis_ExistingKey() {
        String key = "testKey";
        Integer value = 10;
        String shadowKey = "shadow:" + key;

        when(redisTemplate.hasKey(key)).thenReturn(true);

        incrementService.saveOrUpdateDataInRedis(key, value);

        // Expect Long type instead of Integer
        verify(valueOperations).increment(eq(key), eq(Long.valueOf(value)));
        verify(valueOperations).increment(eq(shadowKey), eq(Long.valueOf(value)));
        verify(valueOperations, never()).set(eq(key), eq(value), eq(Duration.ofSeconds(10)));
    }

    @Test
    void testProcessExpiredKey_NoLockAcquired() {
        String expiredKey = "testKey";
        Integer value = 10;
        String shadowKey = "shadow:" + expiredKey;

        when(valueOperations.get(shadowKey)).thenReturn(value);
        when(valueOperations.setIfAbsent(eq(shadowKey), eq(value), eq(Duration.ofSeconds(10))))
                .thenReturn(false);

        incrementService.processExpiredKey(expiredKey);

    }

    @Test
    void testUpdateOrInsertRecord_NewRecord() {
        String expiredKey = "testKey";
        Integer value = 10;

        when(sumRepository.findByKey(expiredKey)).thenReturn(Optional.empty());

        incrementService.updateOrInsertRecord(expiredKey, value);

        ArgumentCaptor<SumEntity> captor = ArgumentCaptor.forClass(SumEntity.class);
        verify(sumRepository).save(captor.capture());
        SumEntity savedEntity = captor.getValue();

        assertEquals(expiredKey, savedEntity.getKey());
        assertEquals(value, savedEntity.getSum());
    }

    @Test
    void testUpdateOrInsertRecord_ExistingRecord() {
        String expiredKey = "testKey";
        Integer value = 10;
        SumEntity existingRecord = SumEntity.builder().key(expiredKey).sum(20).build();

        when(sumRepository.findByKey(expiredKey)).thenReturn(Optional.of(existingRecord));

        incrementService.updateOrInsertRecord(expiredKey, value);

        verify(sumRepository).save(existingRecord);
        assertEquals(30, existingRecord.getSum());
    }
}
