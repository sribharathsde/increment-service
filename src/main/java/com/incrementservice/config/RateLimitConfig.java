package com.incrementservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
public class RateLimitConfig {
    @Value("${increment-config.rate-limit-redis-host}")
    private String rateLimitRedisHost;
    @Value("${increment-config.rate-limit.periods}")
    private Long rateLimitPeriods;
    @Value("${increment-config.rate-limit.requests}")
    private Long rateLimitRequests;

    private RedisClient redisClient() {
        return RedisClient.create(RedisURI.builder()
                                          .withHost(rateLimitRedisHost)
                                          .withPort(6379)
                                          .withSsl(false)
                                          .build());

    }

    @Bean
    public ProxyManager<String> lettuceBasedProxyManager() {
        RedisClient redisClient = redisClient();
        StatefulRedisConnection<String, byte[]> redisConnection = redisClient
                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        return LettuceBasedProxyManager.builderFor(redisConnection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(rateLimitPeriods)))
                .build();
    }

    @Bean
    public Supplier<BucketConfiguration> bucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(rateLimitRequests, Duration.ofMinutes(rateLimitPeriods)))
                .build();
    }
}
