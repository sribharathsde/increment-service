package com.incrementservice.config;

import com.incrementservice.listener.RedisKeyExpirationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configures Redis connections and key expiration listeners.
 */
@Configuration
public class RedisConfig {

    /**
     * Configures the RedisTemplate.
     *
     * @param connectionFactory the Redis connection factory
     * @return the RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    /**
     * Creates the Redis connection factory.
     *
     * @return the RedisConnectionFactory instance
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("increment-redis", 6379));
//        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
    }

    /**
     * Sets up the RedisMessageListenerContainer.
     *
     * @param connectionFactory the Redis connection factory
     * @param listenerAdapter the listener adapter
     * @return the RedisMessageListenerContainer instance
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("__keyevent@0__:expired"));
        return container;
    }

    /**
     * Creates a MessageListenerAdapter for key expiration events.
     *
     * @param listener the RedisKeyExpirationListener instance
     * @return the MessageListenerAdapter instance
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisKeyExpirationListener listener) {

        return new MessageListenerAdapter(listener);
    }
}
